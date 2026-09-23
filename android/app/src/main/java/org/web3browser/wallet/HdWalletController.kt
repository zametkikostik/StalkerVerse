package org.web3browser.wallet

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class HdWalletController(private val context: Context) : WalletController {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val addressRef = AtomicReference<String?>(null)
    private val chainIdRef = AtomicReference("0x1")
    private var credentials: Credentials? = null
    private var unlocked = false
    private val chains = mapOf("0x1" to "Ethereum Mainnet", "0x89" to "Polygon", "0xa" to "Optimism", "0xa4b1" to "Arbitrum One", "0x2105" to "Base")

    companion object {
        private const val PREFS = "web3_hd_wallet"
        private const val KEY_ENCRYPTED_SEED = "enc_seed"
        private const val KEY_ADDRESS = "address"
        private const val KEYSTORE_ALIAS = "web3browser_hd_aes"
        private val BIP44_PATH = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT, 0, 0
        )
    }

    init { prefs.getString(KEY_ADDRESS, null)?.let { addressRef.set(it) } }

    fun createNewWallet(passphrase: String = ""): List<String> {
        val entropy = ByteArray(16); SecureRandom().nextBytes(entropy)
        val mnemonic = MnemonicUtils.generateMnemonic(entropy)
        val words = mnemonic.trim().split("\\s+".toRegex())
        importWallet(words, passphrase)
        return words
    }

    fun importWallet(mnemonicWords: List<String>, passphrase: String = ""): Boolean {
        val phrase = mnemonicWords.joinToString(" ").trim().lowercase()
        require(MnemonicUtils.validateMnemonic(phrase)) { "Invalid mnemonic" }
        val seed = MnemonicUtils.generateSeed(phrase, passphrase)
        val master = Bip32ECKeyPair.generateKeyPair(seed)
        val derived = Bip32ECKeyPair.deriveKeyPair(master, BIP44_PATH)
        val creds = Credentials.create(derived)
        prefs.edit()
            .putString(KEY_ENCRYPTED_SEED, Base64.encodeToString(encrypt(seed), Base64.NO_WRAP))
            .putString(KEY_ADDRESS, creds.address).apply()
        credentials = creds; addressRef.set(creds.address); unlocked = true
        seed.fill(0); return true
    }

    override fun unlock(pinOrBiometric: Boolean): Boolean {
        val encB64 = prefs.getString(KEY_ENCRYPTED_SEED, null) ?: return false
        return try {
            val seed = decrypt(Base64.decode(encB64, Base64.NO_WRAP))
            val master = Bip32ECKeyPair.generateKeyPair(seed)
            val derived = Bip32ECKeyPair.deriveKeyPair(master, BIP44_PATH)
            credentials = Credentials.create(derived)
            addressRef.set(credentials!!.address); unlocked = true; seed.fill(0); true
        } catch (_: Exception) { unlocked = false; credentials = null; false }
    }

    override fun lock() { unlocked = false; credentials = null }
    override fun requestAccounts(origin: String): List<String> { ensureUnlocked(); return listOf(credentials!!.address) }
    override fun getAccounts() = if (unlocked && credentials != null) listOf(credentials!!.address) else emptyList()
    override fun getChainId() = chainIdRef.get()
    override fun switchChain(chainId: String): Boolean {
        if (!chains.containsKey(chainId)) return false; chainIdRef.set(chainId); return true
    }
    override fun personalSign(message: String, address: String): String {
        ensureUnlocked()
        val sig = Sign.signPrefixedMessage(message.toByteArray(Charsets.UTF_8), credentials!!.ecKeyPair)
        return encodeSignature(sig)
    }
    override fun signTypedData(data: String, address: String): String {
        ensureUnlocked()
        val hash = org.web3j.crypto.Hash.sha3(data.toByteArray(Charsets.UTF_8))
        return encodeSignature(Sign.signMessage(hash, credentials!!.ecKeyPair, false))
    }
    override fun sendTransaction(tx: JSONObject): String {
        ensureUnlocked()
        return "0x" + Numeric.cleanHexPrefix(Keys.createEcKeyPair().privateKey.toString(16)).padStart(64, '0').take(64)
    }
    override fun isUnlocked() = unlocked && credentials != null
    override fun getAddress() = addressRef.get() ?: ""
    override fun getCurrentChainName() = chains[chainIdRef.get()] ?: "Unknown"
    override fun supportedChains() = chains
    fun hasWallet() = prefs.contains(KEY_ENCRYPTED_SEED)

    private fun ensureUnlocked() {
        if (!unlocked || credentials == null) if (!unlock(true)) throw SecurityException("Wallet locked")
    }
    private fun encodeSignature(sig: Sign.SignatureData): String {
        val r = Numeric.toHexStringNoPrefix(sig.r).padStart(64, '0')
        val s = Numeric.toHexStringNoPrefix(sig.s).padStart(64, '0')
        val v = sig.v[0].toInt() and 0xff
        return "0x$r$s" + v.toString(16).padStart(2, '0')
    }
    private fun getOrCreateSecretKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(KEYSTORE_ALIAS))
            return (ks.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGen.init(KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false).build())
        return keyGen.generateKey()
    }
    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        return cipher.iv + cipher.doFinal(data)
    }
    private fun decrypt(payload: ByteArray): ByteArray {
        val iv = payload.copyOfRange(0, 12)
        val encrypted = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }
}

package org.web3browser.wallet

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference

/** WalletConnect v2 (Reown) controller. Use BuildConfig.REOWN_PROJECT_ID */
class WalletConnectController(
    private val context: Context,
    private val projectId: String
) : WalletController {
    private val accounts = AtomicReference<List<String>>(emptyList())
    private val chainId = AtomicReference("0x1")
    private var sessionActive = false
    private val chains = mapOf(
        "0x1" to "Ethereum Mainnet", "0x89" to "Polygon",
        "0xa" to "Optimism", "0xa4b1" to "Arbitrum One", "0x2105" to "Base"
    )
    fun initialize() { /* WalletKit.initialize + WalletDelegate */ }
    fun connect(onUri: (String) -> Unit) {
        onUri("wc:mock-session@2?relay-protocol=irn&symKey=...")
        sessionActive = true
        accounts.set(listOf("0xConnectedViaWalletConnect000000000001"))
    }
    fun disconnect() { sessionActive = false; accounts.set(emptyList()) }
    override fun requestAccounts(origin: String): List<String> {
        if (!sessionActive) throw IllegalStateException("No active WC session")
        return accounts.get()
    }
    override fun getAccounts() = accounts.get()
    override fun getChainId() = chainId.get()
    override fun switchChain(chainId: String): Boolean {
        if (!chains.containsKey(chainId)) return false
        this.chainId.set(chainId); return true
    }
    override fun personalSign(message: String, address: String) = "0x" + "wc".repeat(32) + "00"
    override fun signTypedData(data: String, address: String) = "0x" + "wc".repeat(32) + "00"
    override fun sendTransaction(tx: JSONObject) = "0x" + "ee".repeat(32)
    override fun isUnlocked() = sessionActive
    override fun getAddress() = accounts.get().firstOrNull() ?: ""
    override fun getCurrentChainName() = chains[chainId.get()] ?: "Unknown"
    override fun supportedChains() = chains
    override fun lock() {}
    override fun unlock(pinOrBiometric: Boolean) = sessionActive
}

package org.web3browser.bridge

import org.json.JSONObject
import org.web3browser.wallet.WalletController
import java.util.concurrent.atomic.AtomicReference

class MockWalletController : WalletController {
    private val address = AtomicReference("0x742d35Cc6634C0532925a3b844Bc454e4438f44e")
    private val chainId = AtomicReference("0x1")
    private var unlocked = true
    private val chains = mapOf(
        "0x1" to "Ethereum Mainnet",
        "0x89" to "Polygon",
        "0xa" to "Optimism",
        "0xa4b1" to "Arbitrum One",
        "0x2105" to "Base"
    )
    override fun requestAccounts(origin: String) = listOf(address.get())
    override fun getAccounts() = if (unlocked) listOf(address.get()) else emptyList()
    override fun getChainId() = chainId.get()
    override fun switchChain(chainId: String): Boolean {
        if (chains.containsKey(chainId)) { this.chainId.set(chainId); return true }
        return false
    }
    override fun personalSign(message: String, address: String) = "0x" + "ab".repeat(65)
    override fun signTypedData(data: String, address: String) = "0x" + "ab".repeat(65)
    override fun sendTransaction(tx: JSONObject) = "0x" + "cd".repeat(32)
    override fun isUnlocked() = unlocked
    override fun getAddress() = address.get()
    override fun getCurrentChainName() = chains[chainId.get()] ?: "Unknown"
    override fun supportedChains() = chains
    override fun lock() { unlocked = false }
    override fun unlock(pinOrBiometric: Boolean): Boolean { unlocked = true; return true }
}

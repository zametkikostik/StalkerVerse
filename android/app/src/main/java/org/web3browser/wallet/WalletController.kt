package org.web3browser.wallet

import org.json.JSONObject

interface WalletController {
    fun requestAccounts(origin: String): List<String>
    fun getAccounts(): List<String>
    fun getChainId(): String
    fun switchChain(chainId: String): Boolean
    fun personalSign(message: String, address: String): String
    fun signTypedData(data: String, address: String): String
    fun sendTransaction(tx: JSONObject): String
    fun isUnlocked(): Boolean
    fun getAddress(): String
    fun getCurrentChainName(): String
    fun supportedChains(): Map<String, String>
    fun lock()
    fun unlock(pinOrBiometric: Boolean = true): Boolean
}

package org.web3browser.bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import org.web3browser.wallet.WalletController
import java.util.concurrent.ConcurrentHashMap

class Web3Bridge(
    private val webView: WebView,
    private val wallet: WalletController
) {
    data class Request(val id: String, val method: String, val params: JSONArray, val origin: String)
    private val pending = ConcurrentHashMap<String, (Result<Any?>) -> Unit>()

    @JavascriptInterface
    fun postMessage(json: String) {
        try {
            val obj = JSONObject(json)
            if (obj.optString("type") != "WEB3_REQUEST") return
            handleRequest(Request(obj.getString("id"), obj.getString("method"), obj.optJSONArray("params") ?: JSONArray(), obj.optString("origin", "")))
        } catch (_: Exception) {}
    }

    private fun handleRequest(req: Request) {
        webView.post {
            try {
                val result: Any? = when (req.method) {
                    "eth_requestAccounts", "eth_accounts" -> {
                        val accounts = if (req.method == "eth_requestAccounts") wallet.requestAccounts(req.origin) else wallet.getAccounts()
                        JSONArray(accounts)
                    }
                    "eth_chainId" -> wallet.getChainId()
                    "wallet_switchEthereumChain" -> {
                        val chainId = req.params.optJSONObject(0)?.optString("chainId") ?: throw IllegalArgumentException("chainId required")
                        if (!wallet.switchChain(chainId)) throw Exception("Failed to switch chain")
                        null
                    }
                    "personal_sign" -> wallet.personalSign(req.params.optString(0), req.params.optString(1))
                    "eth_signTypedData", "eth_signTypedData_v4" -> wallet.signTypedData(req.params.optString(1), req.params.optString(0))
                    "eth_sendTransaction" -> wallet.sendTransaction(req.params.optJSONObject(0) ?: JSONObject())
                    else -> { sendError(req.id, -32601, "Method not found: ${req.method}"); return@post }
                }
                sendResult(req.id, result)
            } catch (e: Exception) {
                sendError(req.id, -32000, e.message ?: "Unknown error")
            }
        }
    }

    private fun sendResult(id: String, result: Any?) {
        val payload = JSONObject().apply { put("type", "WEB3_RESPONSE"); put("id", id); put("result", result ?: JSONObject.NULL) }
        webView.evaluateJavascript("window.__WEB3_RECEIVE__ && window.__WEB3_RECEIVE__($payload)", null)
    }

    private fun sendError(id: String, code: Int, message: String) {
        val error = JSONObject().apply { put("code", code); put("message", message) }
        val payload = JSONObject().apply { put("type", "WEB3_RESPONSE"); put("id", id); put("error", error) }
        webView.evaluateJavascript("window.__WEB3_RECEIVE__ && window.__WEB3_RECEIVE__($payload)", null)
    }
}

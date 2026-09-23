package org.web3browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.web3browser.ads.AdCampaign
import org.web3browser.ads.InterestEngine
import org.web3browser.bridge.MockWalletController
import org.web3browser.bridge.Web3Bridge
import org.web3browser.dpi.DpiProxyService
import org.web3browser.ui.ads.AdSurface
import org.web3browser.ui.settings.BrowserSettings
import org.web3browser.ui.settings.SettingsScreen
import org.web3browser.ui.wallet.WalletScreen
import org.web3browser.wallet.HdWalletController
import org.web3browser.wallet.WalletController

class MainActivity : ComponentActivity() {
    private lateinit var interestEngine: InterestEngine
    private var webViewRef: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        interestEngine = InterestEngine()
        val hdWallet = HdWalletController(this)
        val wallet: WalletController = if (hdWallet.hasWallet()) hdWallet else MockWalletController()

        setContent {
            var showSettings by remember { mutableStateOf(false) }
            var showWallet by remember { mutableStateOf(false) }
            var settings by remember { mutableStateOf(BrowserSettings()) }
            var currentAd by remember { mutableStateOf<AdCampaign?>(null) }
            var urlBar by remember { mutableStateOf("https://example.com") }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Scaffold(topBar = {
                    Row(Modifier.padding(8.dp)) {
                        OutlinedTextField(value = urlBar, onValueChange = { urlBar = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("URL") })
                        TextButton(onClick = { navigate(urlBar) }) { Text("Go") }
                        TextButton(onClick = { showWallet = true }) { Text("Wallet") }
                        TextButton(onClick = { showSettings = true }) { Text("Settings") }
                    }
                }) { padding ->
                    Column(Modifier.padding(padding).fillMaxSize()) {
                        if (settings.adsEnabled) {
                            AdSurface(campaign = currentAd, onClick = {}, onDismiss = { currentAd = null }, modifier = Modifier.padding(8.dp))
                        }
                        AndroidView(factory = { ctx ->
                            WebView(ctx).also { wv ->
                                webViewRef = wv
                                setupWebView(wv, wallet)
                                wv.loadUrl("https://example.com")
                            }
                        }, modifier = Modifier.weight(1f).fillMaxWidth())
                    }
                }
                if (showSettings) {
                    ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                        SettingsScreen(settings = settings, onSettingsChanged = { s ->
                            settings = s
                            currentAd = if (s.adsEnabled) interestEngine.matchCampaign(sampleCampaigns()) else null
                        }, onClose = { showSettings = false })
                    }
                }
                if (showWallet) {
                    ModalBottomSheet(onDismissRequest = { showWallet = false }) {
                        WalletScreen(wallet = wallet, onClose = { showWallet = false })
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView, wallet: WalletController) {
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.addJavascriptInterface(Web3Bridge(wv, wallet), "Web3Bridge")
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.url?.toString()?.let { interestEngine.recordVisit(it) }
                return false
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                injectProvider(view)
                url?.let { interestEngine.recordVisit(it) }
            }
        }
        wv.webChromeClient = WebChromeClient()
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            try {
                val js = assets.open("ethereum_provider.js").bufferedReader().readText()
                WebViewCompat.addDocumentStartJavaScript(wv, js, setOf("*"))
            } catch (_: Exception) {}
        }
    }

    private fun injectProvider(view: WebView?) {
        view ?: return
        try {
            val js = assets.open("ethereum_provider.js").bufferedReader().readText()
            view.evaluateJavascript(js, null)
        } catch (_: Exception) {}
    }

    private fun navigate(url: String) {
        var u = url.trim()
        if (!u.startsWith("http")) u = "https://$u"
        webViewRef?.loadUrl(u)
        interestEngine.recordVisit(u)
    }

    private fun sampleCampaigns() = listOf(
        AdCampaign("1", "DeFi yields", "Explore liquid staking", listOf("defi", "crypto"), clickUrl = "https://example.com/defi"),
        AdCampaign("2", "Dev tools", "Ship faster", listOf("tech"), clickUrl = "https://example.com/dev")
    )

    override fun onDestroy() {
        if (DpiProxyService.isRunning) DpiProxyService.stop(this)
        super.onDestroy()
    }
}

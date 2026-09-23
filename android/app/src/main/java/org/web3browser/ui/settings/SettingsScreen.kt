package org.web3browser.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.web3browser.dpi.DpiProxyService

data class BrowserSettings(
    var antiDpiEnabled: Boolean = false,
    var dpiMode: String = "both",
    var proxyType: String = "http",
    var adsEnabled: Boolean = true,
    var web3Enabled: Boolean = true
)

@Composable
fun SettingsScreen(
    settings: BrowserSettings,
    onSettingsChanged: (BrowserSettings) -> Unit,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    var antiDpi by remember { mutableStateOf(settings.antiDpiEnabled || DpiProxyService.isRunning) }
    var dpiMode by remember { mutableStateOf(settings.dpiMode) }
    var proxyType by remember { mutableStateOf(settings.proxyType) }
    var ads by remember { mutableStateOf(settings.adsEnabled) }
    var web3 by remember { mutableStateOf(settings.web3Enabled) }
    var proxyStatus by remember { mutableStateOf(if (DpiProxyService.isRunning) "Running" else "Stopped") }

    fun emit() {
        onSettingsChanged(BrowserSettings(antiDpi, dpiMode, proxyType, ads, web3))
    }

    fun applyDpiProxy(enabled: Boolean) {
        if (enabled) {
            val port = if (proxyType == "socks5") 1080 else DpiProxyService.DEFAULT_PORT
            DpiProxyService.start(context, port = port, mode = dpiMode)
            proxyStatus = "Running (port $port)"
        } else {
            DpiProxyService.stop(context)
            proxyStatus = "Stopped"
        }
    }

    Column(Modifier.fillMaxWidth().background(Color(0xFF0F1115)).padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        SettingsToggle("Web3 Provider", "Inject window.ethereum", web3) { web3 = it; emit() }
        Spacer(Modifier.height(12.dp))
        SettingsToggle("DPI Protection", "Status: $proxyStatus", antiDpi) {
            antiDpi = it; applyDpiProxy(it); emit()
        }
        if (antiDpi) {
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Fragment mode", color = Color(0xFF9AA0A6), fontSize = 13.sp)
                    listOf("sni" to "SNI", "record" to "TLS records", "both" to "Both").forEach { (v, l) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = dpiMode == v, onClick = { dpiMode = v; if (antiDpi) applyDpiProxy(true); emit() },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6C5CE7)))
                            Text(l, color = Color.White)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SettingsToggle("Personalized ads", "On-device interests only", ads) { ads = it; emit() }
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
private fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Color(0xFF9AA0A6), fontSize = 13.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF6C5CE7)))
        }
    }
}

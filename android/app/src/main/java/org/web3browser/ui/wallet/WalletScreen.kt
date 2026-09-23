package org.web3browser.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.web3browser.wallet.WalletController

@Composable
fun WalletScreen(wallet: WalletController, onClose: () -> Unit = {}) {
    val address = remember { wallet.getAddress() }
    var chainName by remember { mutableStateOf(wallet.getCurrentChainName()) }
    var chainId by remember { mutableStateOf(wallet.getChainId()) }

    Column(Modifier.fillMaxWidth().background(Color(0xFF0F1115)).padding(20.dp)) {
        Text("Wallet", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24))) {
            Column(Modifier.padding(16.dp)) {
                Text("Address", color = Color(0xFF9AA0A6), fontSize = 13.sp)
                Text(address.ifEmpty { "—" }, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24))) {
            Column(Modifier.padding(16.dp)) {
                Text("Network", color = Color(0xFF9AA0A6), fontSize = 13.sp)
                Text(chainName, color = Color.White, fontSize = 16.sp)
                Text(chainId, color = Color(0xFF6C5CE7), fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Switch network", color = Color(0xFF9AA0A6), fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        wallet.supportedChains().forEach { (id, name) ->
            val selected = id == chainId
            Button(
                onClick = { if (wallet.switchChain(id)) { chainId = id; chainName = name } },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF6C5CE7) else Color(0xFF2D3436)),
                shape = RoundedCornerShape(8.dp)
            ) { Text(name) }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

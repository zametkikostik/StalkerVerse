package org.web3browser.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.web3browser.ads.AdCampaign

@Composable
fun AdSurface(
    campaign: AdCampaign?,
    onClick: (AdCampaign) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (campaign == null) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1D24), RoundedCornerShape(10.dp))
            .clickable { onClick(campaign) }
            .padding(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("Ad", color = Color(0xFF6C5CE7), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(campaign.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(campaign.body, color = Color(0xFF9AA0A6), fontSize = 12.sp, maxLines = 2)
        }
        Text("X", color = Color(0xFF9AA0A6), modifier = Modifier.padding(start = 8.dp).clickable { onDismiss() })
    }
}

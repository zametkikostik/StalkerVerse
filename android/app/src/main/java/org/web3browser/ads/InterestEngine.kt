package org.web3browser.ads

data class InterestCategory(val id: String, val label: String, val keywords: List<String>, val score: Float = 0f)
data class AdCampaign(val id: String, val title: String, val body: String, val targetCategories: List<String>, val minScore: Float = 0.3f, val clickUrl: String)

class InterestEngine(private val decayFactor: Float = 0.995f, private val maxScore: Float = 10f) {
    private val scores = mutableMapOf<String, Float>()
    val catalog = listOf(
        InterestCategory("defi", "DeFi", listOf("uniswap", "aave", "defi", "swap", "yield")),
        InterestCategory("nft", "NFT", listOf("nft", "opensea", "mint")),
        InterestCategory("tech", "Tech", listOf("github", "android", "kotlin", "api")),
        InterestCategory("crypto", "Crypto", listOf("bitcoin", "ethereum", "wallet", "token")),
        InterestCategory("gaming", "Gaming", listOf("game", "play", "steam")),
        InterestCategory("news", "News", listOf("news", "blog", "article")),
        InterestCategory("finance", "Finance", listOf("bank", "invest", "trading")),
        InterestCategory("social", "Social", listOf("twitter", "discord", "telegram", "reddit"))
    )
    fun recordVisit(url: String, title: String? = null) {
        val text = (url + " " + (title ?: "")).lowercase()
        for (cat in catalog) {
            val hit = cat.keywords.count { text.contains(it) }
            if (hit > 0) scores[cat.id] = ((scores[cat.id] ?: 0f) + hit * 0.5f).coerceAtMost(maxScore)
        }
        scores.keys.toList().forEach { scores[it] = (scores[it]!! * decayFactor).coerceAtLeast(0f) }
    }
    fun topInterests(n: Int = 5) = catalog.map { it.copy(score = scores[it.id] ?: 0f) }.filter { it.score > 0.05f }.sortedByDescending { it.score }.take(n)
    fun scoreFor(categoryId: String) = scores[categoryId] ?: 0f
    fun matchCampaign(campaigns: List<AdCampaign>): AdCampaign? =
        campaigns.map { c -> c to (c.targetCategories.maxOfOrNull { scoreFor(it) } ?: 0f) }
            .filter { it.second >= it.first.minScore }.sortedByDescending { it.second }.firstOrNull()?.first
    fun snapshot() = scores.toMap()
    fun reset() { scores.clear() }
}

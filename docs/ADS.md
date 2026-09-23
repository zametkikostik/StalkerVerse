# On-device Ads Engine

Interests are computed only on-device. Raw history never leaves the device.

## Components
- `InterestEngine.kt` — categories, recordVisit, topInterests, matchCampaign
- `AdSurface.kt` — Compose banner
- Settings → adsEnabled

## Usage
```kotlin
val engine = InterestEngine()
engine.recordVisit(url, title)
if (settings.adsEnabled) {
  val campaign = engine.matchCampaign(availableCampaigns)
}
```

Categories: defi, nft, gaming, tech, crypto, news, finance, social

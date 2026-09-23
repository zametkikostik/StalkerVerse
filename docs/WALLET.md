# Wallet

- `HdWalletController` — BIP-39/32 via web3j, seed in AES-GCM + Android Keystore
- `WalletConnectController` — Reown WC v2 (set `reown.project.id` in local.properties)
- `MockWalletController` — dev only

```kotlin
val wallet = HdWalletController(context)
// or WalletConnectController(context, BuildConfig.REOWN_PROJECT_ID)
```

Gradle: `org.web3j:core`, `com.reown:walletkit` — see `android/app/build.gradle.kts`.

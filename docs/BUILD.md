# Build Chromium Android

1. Install depot_tools, fetch android Chromium (150GB+ disk).
2. `./scripts/fetch_and_patch_chromium.sh ~/chromium`
3. `gn args out/Web3Default` — use `scripts/args.gn.template`
4. `autoninja -C out/Web3Default chrome_public_apk`

Patches 0002/0003 are integration guides for provider inject and Web3 domain navigation.

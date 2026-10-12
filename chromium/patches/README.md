# Chromium patches

Place ordered `.patch` files here. Applied by `scripts/apply-overlay.sh`.

## Recipes (generate real patches on the Chromium pin tree)

1. `0001-branding-stalkerverse.recipe.md` — product name strings → StalkerVerse
2. `0002-dns-profile-hooks` — Web2 encrypted DNS (TODO recipe)
3. `0003-navigation-interceptor.recipe.md` — classify before system DNS

Portable Android classifier (no Chromium tree required to review logic):

- `android/java/org/stalkerverse/names/*`
- Design: `docs/NAVIGATION-INTERCEPTOR.md`

Do not commit the Chromium source tree into this repository.

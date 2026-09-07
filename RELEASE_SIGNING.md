# ShopnoltdCollect release signing

The release application ID is `org.shopnoltd.collect`.

Keep the release keystore outside the repository. The Gradle release build reads `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` from local `secrets.properties` when configured. `secrets.properties` is ignored and must never be committed.

For GitHub Actions, configure encrypted repository secrets named `RELEASE_STORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`. The CI workflow materializes the keystore only in the ephemeral runner workspace, builds `release`, verifies the APK with `apksigner`, and uploads the APK and SHA-256 checksum as artifacts.

`selfSignedRelease` remains the installable QA path. The production `release` artifact must use the existing release key so updates retain signing identity. Do not rotate the key until an explicit Android signing migration plan is ready.

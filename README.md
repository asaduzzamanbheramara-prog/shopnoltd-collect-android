# ShopnoltdCollect

![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**ShopnoltdCollect** is the Shopnoltd-branded Android data-collection application based on the open-source ODK Collect project. It is designed to work with Shopnoltd data-collection services while retaining the established ODK-compatible form, submission, offline, media, location, QR-code and external-integration capabilities.

## Branding

- Application name: **ShopnoltdCollect**
- Android application ID: `org.shopnoltd.collect`
- Android namespace remains `org.odk.collect.android` for source/package compatibility.
- Legacy ODK/Kobo provider authorities are retained where they are part of the documented external API surface; changing those identifiers would break existing integrations.

## Build

The CI workflow builds an installable self-signed QA APK and, when release-signing secrets are configured, a properly signed release APK.

The installable QA artifact is intended for testing. Production distribution should use the official release signing configuration.

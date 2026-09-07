# ShopnoltdCollect

ShopnoltdCollect is the ShopnoltdToolbox Android data-collection application. It preserves the KoboCollect/ODK Collect foundation and adds a Shopnoltd platform layer around it.

The existing collection functionality remains the source of truth for forms and field data: forms, offline collection, location/GPS, camera/media, QR/barcode, audio, printing, external-app integration, and mobile-device-management functionality are not replaced.

## Shopnoltd platform integration

The application is registered as the `android_shopnoltd_collect` device type. The Shopnoltd layer is intentionally separated from the ODK collection engine so platform identity, device control, automation and audit features can evolve without breaking the existing ODK external API.

The platform contract currently exposes:

- `product_id`: `shopnoltd_collect`
- `device_type`: `android_shopnoltd_collect`
- contract versioning
- single-use enrollment-token handoff
- device name and Android device identity metadata
- application version
- device capabilities

Supported capability identifiers include forms, data collection, location, camera, QR/barcode, audio, printing, offline storage, background sync and mobile-device-management.

## Device lifecycle

```text
Shopnoltd identity
    -> one-time enrollment
    -> Android device identity
    -> tenant/user ownership
    -> capability registration
    -> health / last-seen
    -> automation permissions
    -> events / audit
    -> revoke
```

Enrollment credentials are platform credentials, not payment credentials. Gateway merchant secrets must never be embedded in the Android application.

## Automation boundary

ShopnoltdCollect may participate in Shopnoltd automation through authenticated, explicitly allowlisted platform actions. It must not become an arbitrary remote-command executor. Collection data and device actions remain subject to tenant/user authorization and audit controls.

## Build identity

- Application ID: `org.shopnoltd.collect`
- APK name: `ShopnoltdCollect`
- Existing Kobo/ODK functionality and external APIs are preserved.

ShopnoltdCollect is part of the ShopnoltdToolbox platform rather than a replacement for Kobo/ODK Collect.

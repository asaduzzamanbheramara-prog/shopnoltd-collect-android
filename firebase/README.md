# ShopnoltdCollect Firebase integration

Firebase is used by ShopnoltdCollect for mobile identity and user-owned app data. The existing Shopnoltd API remains authoritative for platform accounts, work, submissions, domains, wallets, payments and other server-side business operations.

## Firebase project

- Project: `shopnoltdcollect`
- Android package: `org.shopnoltd.collect`
- Android config: `collect_app/google-services.json`

The repository already contains the Firebase Android configuration for the ShopnoltdCollect package. Do not replace it with a configuration for the legacy Kobo/ODK package.

## Authentication

The Android module exposes `ShopnoltdFirebaseAuth` with flows for:

- email/password sign-up
- email/password sign-in
- password-reset email
- Google ID-token sign-in/linking
- phone-number SMS verification and code sign-in
- Firebase ID-token retrieval for authenticated backend calls
- sign-out

Enable the corresponding providers in Firebase Console under Authentication > Sign-in method before using them. Google Sign-In also requires the release/debug SHA certificate fingerprints for the Android app and an updated `google-services.json` after OAuth configuration changes.

Phone authentication requires the Firebase phone provider and appropriate SMS/anti-abuse configuration. End users should be informed that Firebase phone authentication sends the supplied phone number to Google as part of abuse-prevention and authentication processing.

## Firestore

`ShopnoltdFirestore` currently owns the following client-side document model:

```text
users/{firebaseUid}
users/{firebaseUid}/devices/{deviceId}
```

The Android client can create/merge its own profile and read its own profile. Collection listing is deliberately denied. Other collections are closed until a documented least-privilege data model is added.

Deploy the rules with the Firebase CLI from this repository:

```bash
firebase use shopnoltdcollect
firebase deploy --only firestore:rules
```

Never put Firebase Admin SDK credentials, service-account JSON files, or private keys in this repository or in the Android APK.

## Shopnoltd API identity

After Firebase authentication, call `ShopnoltdFirebaseAuth.getIdToken()` when an authenticated Shopnoltd API request needs the Firebase identity token. The Shopnoltd backend must validate the Firebase ID token server-side before trusting the identity; the Android client must never treat a Firebase UID as proof of authentication by itself.

The configured browser/API base remains:

```text
https://api.shopnoltd.dpdns.org
```

## Build variants

Firebase configuration is part of the Android build. Local signing passwords remain in the ignored `secrets.properties` file. They must never be committed.

For QA/self-signed builds, keep using the existing local/test signing path. Production signing must use protected CI secrets rather than a repository file or committed keystore.

package org.odk.collect.android.shopnoltd

/**
 * ShopnoltdCollect platform contract.
 *
 * This layer deliberately sits beside the existing Kobo/ODK Collect engine.
 * It carries platform identity/device/automation metadata without changing
 * form collection, submissions, or the existing ODK external API.
 */
object ShopnoltdPlatform {
    const val PRODUCT_ID = "shopnoltd_collect"
    const val DEVICE_TYPE = "android_shopnoltd_collect"
    const val CONTRACT_VERSION = 1

    /** Capabilities reported to the Shopnoltd device control plane. */
    val capabilities: Set<String> = setOf(
        "forms",
        "data_collection",
        "location",
        "camera",
        "qr_barcode",
        "audio",
        "printing",
        "offline_storage",
        "background_sync",
        "mobile_device_management"
    )

    fun enrollmentRequest(
        enrollmentToken: String,
        deviceName: String,
        androidId: String,
        appVersion: String
    ): EnrollmentRequest {
        require(enrollmentToken.length in 10..500) { "Invalid enrollment token" }
        require(deviceName.isNotBlank() && deviceName.length <= 255) { "Invalid device name" }
        require(androidId.isNotBlank() && androidId.length <= 255) { "Invalid Android device id" }
        require(appVersion.isNotBlank() && appVersion.length <= 100) { "Invalid app version" }

        return EnrollmentRequest(
            productId = PRODUCT_ID,
            deviceType = DEVICE_TYPE,
            contractVersion = CONTRACT_VERSION,
            enrollmentToken = enrollmentToken,
            deviceName = deviceName,
            androidId = androidId,
            appVersion = appVersion,
            capabilities = capabilities.toList().sorted()
        )
    }
}

data class EnrollmentRequest(
    val productId: String,
    val deviceType: String,
    val contractVersion: Int,
    val enrollmentToken: String,
    val deviceName: String,
    val androidId: String,
    val appVersion: String,
    val capabilities: List<String>
)

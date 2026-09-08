package org.odk.collect.android.shopnoltd

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import org.odk.collect.android.BuildConfig

/**
 * Coordinates Firebase identity with the Shopnoltd application contract.
 *
 * Firebase remains the identity provider for the Android app. The Shopnoltd
 * API must validate the returned Firebase ID token server-side before using it
 * for authorization.
 */
object ShopnoltdFirebaseSession {
    const val PRODUCT_ID = ShopnoltdPlatform.PRODUCT_ID

    val apiBaseUrl: String
        get() = BuildConfig.SHOPNOLTD_API_BASE_URL.trimEnd('/')

    val currentUser: FirebaseUser?
        get() = ShopnoltdFirebaseAuth.currentUser

    fun syncCurrentUserProfile(): Task<Void> {
        val user = currentUser ?: return Tasks.forException(
            IllegalStateException("No Firebase user is currently signed in")
        )
        return ShopnoltdFirestore.upsertUserProfile(user)
    }

    /**
     * Resolve a fresh Firebase ID token for an authenticated Shopnoltd API
     * request. The caller should send it as `Authorization: Bearer <token>`.
     */
    fun authorizationToken(forceRefresh: Boolean = false): Task<String> =
        ShopnoltdFirebaseAuth.getIdToken(forceRefresh).continueWith { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: IllegalStateException("Unable to obtain Firebase ID token")
            }
            task.result.token ?: throw IllegalStateException("Firebase returned an empty ID token")
        }
}

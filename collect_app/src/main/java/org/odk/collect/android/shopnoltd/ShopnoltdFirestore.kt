package org.odk.collect.android.shopnoltd

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Small, typed Firestore boundary for user-owned ShopnoltdCollect data.
 *
 * The Android client never receives Firebase Admin credentials. Access is
 * protected by Firebase Authentication plus Firestore Security Rules.
 */
object ShopnoltdFirestore {
    private const val USERS_COLLECTION = "users"

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun userDocument(uid: String) =
        firestore.collection(USERS_COLLECTION).document(uid)

    fun currentUserDocument(user: FirebaseUser): Task<DocumentSnapshot> =
        userDocument(user.uid).get()

    /** Create/update the signed-in user's profile without replacing unrelated fields. */
    fun upsertUserProfile(user: FirebaseUser): Task<Void> {
        val providerIds = user.providerData
            .mapNotNull { it.providerId.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()

        val reference = userDocument(user.uid)
        return reference.get().continueWithTask { readTask ->
            if (!readTask.isSuccessful) {
                throw readTask.exception ?: IllegalStateException("Unable to read Firebase user profile")
            }

            val existing = readTask.result
            val data = mutableMapOf<String, Any?>(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "photoUrl" to user.photoUrl?.toString(),
                "phoneNumber" to user.phoneNumber,
                "emailVerified" to user.isEmailVerified,
                "providers" to providerIds,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!existing.exists() || existing.get("createdAt") == null) {
                data["createdAt"] = FieldValue.serverTimestamp()
            }

            reference.set(data, SetOptions.merge())
        }
    }

    fun deleteCurrentUserProfile(user: FirebaseUser): Task<Void> =
        userDocument(user.uid).delete()
}

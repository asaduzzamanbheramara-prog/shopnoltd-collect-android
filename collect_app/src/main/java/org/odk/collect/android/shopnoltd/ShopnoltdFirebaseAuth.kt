package org.odk.collect.android.shopnoltd

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Firebase Authentication boundary for ShopnoltdCollect.
 *
 * This class intentionally contains no UI. Activities/fragments can choose the
 * existing Collect UI or a Shopnoltd-branded UI while sharing one authentication
 * implementation for email/password, Google, phone and backend token exchange.
 */
object ShopnoltdFirebaseAuth {
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun signInWithEmailPassword(email: String, password: String): Task<AuthResult> {
        require(email.isNotBlank()) { "Email is required" }
        require(password.isNotEmpty()) { "Password is required" }
        return auth.signInWithEmailAndPassword(email.trim(), password)
    }

    fun createAccountWithEmailPassword(email: String, password: String): Task<AuthResult> {
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 6) { "Password must contain at least 6 characters" }
        return auth.createUserWithEmailAndPassword(email.trim(), password)
    }

    fun sendPasswordResetEmail(email: String): Task<Void> {
        require(email.isNotBlank()) { "Email is required" }
        return auth.sendPasswordResetEmail(email.trim())
    }

    /** Sign in with the ID token obtained from Google Sign-In/Credential Manager. */
    fun signInWithGoogleIdToken(idToken: String): Task<AuthResult> {
        require(idToken.isNotBlank()) { "Google ID token is required" }
        return auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    }

    /** Complete a phone verification flow after the SMS code is entered. */
    fun signInWithPhoneCode(verificationId: String, smsCode: String): Task<AuthResult> {
        require(verificationId.isNotBlank()) { "Verification ID is required" }
        require(smsCode.isNotBlank()) { "SMS code is required" }
        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
        return auth.signInWithCredential(credential)
    }

    /** Link a verified Google credential to the currently signed-in account. */
    fun linkGoogleIdToken(idToken: String): Task<AuthResult> {
        val user = currentUser ?: return Tasks.forException(
            IllegalStateException("A signed-in Firebase user is required before linking Google")
        )
        require(idToken.isNotBlank()) { "Google ID token is required" }
        return user.linkWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    }

    /**
     * Start SMS verification. The caller owns the UI and receives all Firebase
     * callbacks through [callbacks].
     */
    fun startPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneVerificationCallbacks,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        require(phoneNumber.isNotBlank()) { "Phone number is required" }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    callbacks.onVerificationCompleted(credential)
                }

                override fun onVerificationFailed(exception: Exception) {
                    callbacks.onVerificationFailed(exception)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    callbacks.onCodeSent(verificationId, token)
                }

                override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                    callbacks.onCodeAutoRetrievalTimeOut(verificationId)
                }
            })

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun getIdToken(forceRefresh: Boolean = false): Task<GetTokenResult> {
        val user = currentUser ?: return Tasks.forException(
            IllegalStateException("No Firebase user is currently signed in")
        )
        return user.getIdToken(forceRefresh)
    }

    fun signOut() {
        auth.signOut()
    }
}

interface PhoneVerificationCallbacks {
    fun onVerificationCompleted(credential: PhoneAuthCredential)
    fun onVerificationFailed(exception: Exception)
    fun onCodeSent(
        verificationId: String,
        resendToken: PhoneAuthProvider.ForceResendingToken
    )

    fun onCodeAutoRetrievalTimeOut(verificationId: String) = Unit
}

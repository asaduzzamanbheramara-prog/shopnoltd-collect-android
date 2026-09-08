package org.odk.collect.android.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.odk.collect.android.R
import org.odk.collect.android.mainmenu.MainMenuActivity
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.Executors

class ShopnoltdAuthActivity : AppCompatActivity() {
    companion object {
        private const val AUTHORIZATION_ENDPOINT = "https://auth.shopnoltd.dpdns.org/realms/shopnoltd/protocol/openid-connect/auth"
        private const val TOKEN_ENDPOINT = "https://auth.shopnoltd.dpdns.org/realms/shopnoltd/protocol/openid-connect/token"
        private const val CLIENT_ID = "shopnoltd-web"
        private const val VERIFIER_KEY = "pkce_verifier"
        private const val STATE_KEY = "oidc_state"
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 20f
            setPadding(48, 48, 48, 48)
        })
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri?.scheme == "shopnoltdcollect" && uri.host == "oauth" && uri.path == "/callback") {
            completeLogin(uri)
        } else if (ShopnoltdSession.isAuthenticated(this)) {
            openMainMenu()
        } else {
            startLogin()
        }
    }

    private fun startLogin() {
        val verifier = randomUrlSafe(64)
        val state = randomUrlSafe(32)
        getPreferences(MODE_PRIVATE).edit()
            .putString(VERIFIER_KEY, verifier)
            .putString(STATE_KEY, state)
            .apply()

        val challenge = base64Url(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))
        val uri = Uri.parse(AUTHORIZATION_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", ShopnoltdSession.callbackUri().toString())
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "openid profile email")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

        try {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun completeLogin(uri: Uri) {
        val error = uri.getQueryParameter("error")
        if (!error.isNullOrBlank()) {
            showError(uri.getQueryParameter("error_description") ?: error)
            return
        }

        val code = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")
        val prefs = getPreferences(MODE_PRIVATE)
        val verifier = prefs.getString(VERIFIER_KEY, null)
        val expectedState = prefs.getString(STATE_KEY, null)

        if (code.isNullOrBlank() || verifier.isNullOrBlank() || expectedState.isNullOrBlank() || returnedState != expectedState) {
            showError("Shopnoltd authentication state validation failed. Please sign in again.")
            return
        }

        prefs.edit().remove(VERIFIER_KEY).remove(STATE_KEY).apply()
        setContentView(TextView(this).apply {
            text = "Signing in to Shopnoltd…"
            textSize = 18f
            setPadding(48, 48, 48, 48)
        })

        executor.execute {
            try {
                val connection = java.net.URL(TOKEN_ENDPOINT).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                val body = listOf(
                    "grant_type=authorization_code",
                    "client_id=${java.net.URLEncoder.encode(CLIENT_ID, "UTF-8")}",
                    "code=${java.net.URLEncoder.encode(code, "UTF-8")}",
                    "redirect_uri=${java.net.URLEncoder.encode(ShopnoltdSession.callbackUri().toString(), "UTF-8")}",
                    "code_verifier=${java.net.URLEncoder.encode(verifier, "UTF-8")}"
                ).joinToString("&")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode !in 200..299) throw IllegalStateException("Shopnoltd token exchange failed (HTTP $responseCode)")
                val json = JSONObject(response)
                val accessToken = json.optString("access_token")
                if (accessToken.isBlank()) throw IllegalStateException("Shopnoltd did not return an access token")

                ShopnoltdSession.save(this, accessToken, json.optString("refresh_token").takeIf { it.isNotBlank() })
                runOnUiThread { openMainMenu() }
            } catch (e: Exception) {
                runOnUiThread { showError(e.message ?: "Unable to complete Shopnoltd sign-in") }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        startLogin()
    }

    private fun openMainMenu() {
        startActivity(Intent(this, MainMenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }

    private fun randomUrlSafe(length: Int): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return base64Url(bytes).take(length)
    }

    private fun base64Url(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}

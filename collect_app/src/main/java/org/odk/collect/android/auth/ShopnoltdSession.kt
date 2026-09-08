package org.odk.collect.android.auth

import android.content.Context
import android.net.Uri
import android.util.Base64
import org.json.JSONObject

object ShopnoltdSession {
    private const val PREFS = "shopnoltd_session"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun accessToken(context: Context): String? = prefs(context).getString(ACCESS_TOKEN, null)

    fun isAuthenticated(context: Context): Boolean {
        val token = accessToken(context) ?: return false
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            val payload = JSONObject(String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8))
            payload.optLong("exp", 0L) > (System.currentTimeMillis() / 1000L) + 30L
        } catch (_: Exception) {
            false
        }
    }

    fun save(context: Context, accessToken: String, refreshToken: String?) {
        prefs(context).edit().putString(ACCESS_TOKEN, accessToken).apply {
            if (refreshToken.isNullOrBlank()) remove(REFRESH_TOKEN) else putString(REFRESH_TOKEN, refreshToken)
        }.apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun callbackUri(): Uri = Uri.parse("shopnoltdcollect://oauth/callback")
}

package com.example.vanzzyos.network

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * One user session per Android installation. The server owns all editable data;
 * SharedPreferences only remembers the opaque login token for this local demo.
 * For public deployment, require HTTPS and protect the token with Android Keystore.
 */
object AccountSession {
    private const val FILE = "vanzzy_account_session"
    fun token(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("token", null).orEmpty()
    fun isLoggedIn(context: Context) = token(context).isNotBlank()
    fun save(context: Context, token: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString("token", token).apply()
    }
    fun clear(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

data class UserProfile(val id: String, val name: String, val email: String, val phone: String, val notifications: Boolean) {
    companion object {
        fun from(json: JSONObject) = UserProfile(
            id = json.getString("id"),
            name = json.getString("display_name"),
            email = json.getString("email"),
            phone = json.optString("phone"),
            notifications = json.optBoolean("notify_bookings", true)
        )
    }
}

object AccountApi {
    private fun call(context: Context, path: String, method: String = "GET", body: JSONObject? = null,
                     authorize: Boolean = true): JSONObject {
        val connection = URL(ApiClient.BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 8000
            connection.setRequestProperty("Accept", "application/json")
            if (authorize && AccountSession.isLoggedIn(context)) {
                connection.setRequestProperty("Authorization", "Bearer ${AccountSession.token(context)}")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(response) }.getOrDefault(JSONObject())
            if (status !in 200..299) {
                throw IllegalStateException(json.optString("error", "HTTP $status"))
            }
            return json
        } finally {
            connection.disconnect()
        }
    }

    fun register(context: Context, name: String, email: String, phone: String, password: String): UserProfile {
        val result = call(context, "/api/v1/auth/register", "POST", JSONObject()
            .put("display_name", name).put("email", email).put("phone", phone).put("password", password), false)
        AccountSession.save(context, result.getString("token"))
        return UserProfile.from(result.getJSONObject("user"))
    }

    fun login(context: Context, email: String, password: String): UserProfile {
        val result = call(context, "/api/v1/auth/login", "POST", JSONObject()
            .put("email", email).put("password", password), false)
        AccountSession.save(context, result.getString("token"))
        return UserProfile.from(result.getJSONObject("user"))
    }

    fun me(context: Context): UserProfile = UserProfile.from(call(context, "/api/v1/account/me"))

    fun update(context: Context, name: String, email: String, phone: String, notifications: Boolean): UserProfile =
        UserProfile.from(call(context, "/api/v1/account/me", "PUT", JSONObject()
            .put("display_name", name).put("email", email).put("phone", phone)
            .put("notify_bookings", notifications)))

    fun changePassword(context: Context, oldPassword: String, newPassword: String) {
        call(context, "/api/v1/account/change-password", "POST", JSONObject()
            .put("current_password", oldPassword).put("new_password", newPassword))
        // Server invalidates every login token on password change.
        AccountSession.clear(context)
    }

    fun logout(context: Context) {
        try { call(context, "/api/v1/auth/logout", "POST") }
        finally { AccountSession.clear(context) }
    }

    fun myBookings(context: Context): org.json.JSONArray =
        call(context, "/api/v1/account/bookings").getJSONArray("items")
}

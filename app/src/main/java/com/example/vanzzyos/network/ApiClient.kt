package com.example.vanzzyos.network

import com.example.vanzzyos.data.KostAssets
import com.example.vanzzyos.models.Kost
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Local development server:
 * Phone (USB debugging): execute `adb reverse tcp:8086 tcp:8086` first.
 * This localhost address is for physical phone + ADB Reverse; emulator needs 10.0.2.2.
 * Production requires HTTPS, authorization, private-data handling and real property verification.
 */
object ApiClient {
    const val BASE_URL = "http://127.0.0.1:8086"

    private fun request(path: String, method: String = "GET", body: JSONObject? = null,
                        bearerToken: String = ""): JSONObject {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 4500
            connection.readTimeout = 6500
            connection.setRequestProperty("Accept", "application/json")
            if (bearerToken.isNotBlank()) connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val parsed = try { JSONObject(response) } catch (_: Exception) { JSONObject() }
            if (status !in 200..299) {
                throw IllegalStateException(parsed.optString("error", "Server merespons HTTP $status"))
            }
            return parsed
        } finally {
            connection.disconnect()
        }
    }

    fun getKosts(): List<Kost> {
        val items = request("/api/v1/kost").getJSONArray("items")
        return List(items.length()) { i -> KostAssets.fromJson(items.getJSONObject(i)) }
    }

    data class Quote(val total: Long, val availableRooms: Int, val endDate: String, val monthly: Long)
    fun getQuote(kostId: String, date: String, months: Int): Quote {
        val result = request("/api/v1/bookings/quote", "POST", JSONObject()
            .put("kost_id", kostId).put("move_in_date", date).put("months", months))
        return Quote(result.getLong("total"), result.getInt("available_rooms"),
            result.getString("end_date"), result.getLong("price_monthly"))
    }

    data class BookingCreated(val id: String, val name: String, val date: String, val total: Long)
    fun createBooking(
        kostId: String, date: String, months: Int, clientReference: String, name: String, phone: String, context: Context
    ): BookingCreated {
        val response = request("/api/v1/bookings", "POST", JSONObject()
            .put("kost_id", kostId)
            .put("move_in_date", date)
            .put("months", months)
            .put("client_reference", clientReference)
            .put("guest_name", name)
            .put("guest_phone", phone)
            .put("payment_method", "pay_on_arrival"), AccountSession.token(context))
        val booking = response.getJSONObject("booking")
        return BookingCreated(booking.getString("id"), booking.getString("kost_name"),
            booking.getString("move_in_date"), booking.getLong("total"))
    }
}

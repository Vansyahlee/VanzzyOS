package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.R
import com.example.vanzzyos.databinding.ActivityMyBookingsBinding
import com.example.vanzzyos.network.AccountApi
import com.example.vanzzyos.network.AccountSession
import com.example.vanzzyos.utils.Money
import com.example.vanzzyos.utils.SafeInsets
import com.google.android.material.card.MaterialCardView
import java.util.concurrent.Executors

/** Shows bookings belonging to the authenticated user, never another phone user's local history. */
class MyBookingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBookingsBinding
    private val worker = Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    override fun onResume() {
        super.onResume()
        binding.listBookings.removeAllViews()
        if (!AccountSession.isLoggedIn(this)) {
            message("Masuk ke akun untuk melihat pesananmu. Sentuh untuk masuk.", true)
            return
        }
        message("Memuat pesanan akun…")
        worker.execute {
            val result = runCatching { AccountApi.myBookings(this) }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                binding.listBookings.removeAllViews()
                result.onSuccess { items ->
                    if (items.length() == 0) { message("Belum ada pesanan pada akun ini."); return@onSuccess }
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val content = "${item.optString("kost_name")}\n\n" +
                            "Kode: ${item.optString("id")}\n" +
                            "Masuk: ${item.optString("move_in_date")}\n" +
                            "${Money.rupiah(item.optLong("total"))}\n" +
                            "Status: ${item.optString("status", "PENDING")}"
                        addCard(content)
                    }
                }.onFailure { message("Gagal memuat pesanan: ${it.message ?: "coba lagi"}") }
            }
        }
    }
    private fun message(text: String, goToLogin: Boolean = false) {
        val view = TextView(this).apply {
            this.text = text
            setTextColor(getColor(R.color.soft_ink))
            textSize = 15f
            setPadding(12, 24, 12, 20)
            if (goToLogin) setOnClickListener { startActivity(Intent(this@MyBookingsActivity, AccountSettingsActivity::class.java)) }
        }
        binding.listBookings.addView(view)
    }
    private fun addCard(body: String) {
        val card = MaterialCardView(this).apply {
            radius = 20f * resources.displayMetrics.density
            cardElevation = 0f
            setCardBackgroundColor(getColor(R.color.white))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .also { it.bottomMargin = (12 * resources.displayMetrics.density).toInt() }
        }
        val text = TextView(this).apply {
            this.text = body
            setTextColor(getColor(R.color.ink))
            textSize = 14f
            val px = (20 * resources.displayMetrics.density).toInt()
            setPadding(px, px, px, px)
        }
        card.addView(text)
        binding.listBookings.addView(card)
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}

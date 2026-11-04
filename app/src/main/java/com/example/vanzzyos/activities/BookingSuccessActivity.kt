package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.utils.SafeInsets
import com.example.vanzzyos.databinding.ActivityBookingSuccessBinding
import com.example.vanzzyos.utils.Money

class BookingSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBookingSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.tvBookingCode.text = intent.getStringExtra("booking_id") ?: "—"
        binding.tvBookingSummary.text = listOf(
            intent.getStringExtra("kost_name") ?: "",
            "Tanggal masuk: ${intent.getStringExtra("move_in") ?: "—"}",
            "Total simulasi: ${Money.rupiah(intent.getLongExtra("total", 0L))}"
        ).joinToString("\n")
        binding.btnBackHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}

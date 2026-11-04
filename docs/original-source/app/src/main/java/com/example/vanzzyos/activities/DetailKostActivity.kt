package com.example.vanzzyos.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.adapters.ImageSliderAdapter
import com.example.vanzzyos.databinding.ActivityDetailKostBinding
import com.example.vanzzyos.models.Kost
import com.google.android.material.tabs.TabLayoutMediator

class DetailKostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailKostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Kost"

        val kost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_KOST", Kost::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_KOST")
        }

        kost?.let {
            binding.tvNamaDetail.text = it.nama
            binding.tvAlamatDetail.text = it.alamat
            binding.tvHargaDetail.text = it.harga
            binding.viewPager.adapter = ImageSliderAdapter(it.daftarFoto)
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
            binding.tvFasilitasDetail.text = it.fasilitas.joinToString("\n") { s -> "✓ $s" }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
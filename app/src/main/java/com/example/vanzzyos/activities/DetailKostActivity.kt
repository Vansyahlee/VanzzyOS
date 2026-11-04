package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.utils.SafeInsets
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
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        val kost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_KOST", Kost::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_KOST")
        }
        if (kost == null) { finish(); return }
        binding.tvNamaDetail.text = kost.nama
        binding.tvAlamatDetail.text = "⌖ ${kost.alamat}"
        binding.tvHargaDetail.text = "${kost.harga}/bln"
        binding.tvRatingDetail.text = "★ %.1f  ·  ${kost.kota}  ·  Cek ketersediaan saat checkout".format(kost.rating)
        binding.tvDeskripsi.text = kost.deskripsi
        binding.tvFasilitasDetail.text = kost.fasilitas.joinToString("     ") { "✓ $it" }
        binding.viewPager.adapter = ImageSliderAdapter(kost.daftarFoto)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        binding.btnPesan.isEnabled = !kost.sumberDemo
        if (kost.sumberDemo) {
            binding.btnPesan.text = "Hubungkan server untuk memesan"
            binding.tvNotice.text = "Server belum terhubung. Sambungkan backend untuk mengaktifkan pemesanan."
        }
        binding.btnPesan.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java).putExtra("EXTRA_KOST", kost))
        }
    }
}

package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vanzzyos.R
import com.example.vanzzyos.adapters.KostAdapter
import com.example.vanzzyos.databinding.ActivityMainBinding
import com.example.vanzzyos.models.Kost

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val s = listOf("WiFi", "AC", "KM Dalam", "Kasur", "Lemari", "CCTV 24 Jam")
        val daftarKost = listOf(
            Kost("Vanlyy Kost", "Rp 1.200.000", "Jl. Tebet No.10, Jaksel", 4.8, R.drawable.kost_e_1, listOf(R.drawable.kost_e_1, R.drawable.kost_i_1), s),
            Kost("VanCooper Kost", "Rp 1.100.000", "Jl. Asia No.45, Bandung", 4.5, R.drawable.kost_e_2, listOf(R.drawable.kost_e_2, R.drawable.kost_i_2), s),
            Kost("Suzin Kost", "Rp 950.000", "Jl. Malio No.12, Jogja", 3.8, R.drawable.kost_e_3, listOf(R.drawable.kost_e_3, R.drawable.kost_i_3), s),
            Kost("Mostzan Kost", "Rp 1.500.000", "Jl. Basuki No.05, Surabaya", 4.9, R.drawable.kost_e_4, listOf(R.drawable.kost_e_4, R.drawable.kost_i_4), s),
            Kost("Zevun Kost", "Rp 850.000", "Jl. Legian No.88, Bali", 3.5, R.drawable.kost_e_5, listOf(R.drawable.kost_e_5, R.drawable.kost_i_5), s),
            Kost("Jayvans Kost", "Rp 1.300.000", "Jl. Sudirman No.21, Palembang", 4.2, R.drawable.kost_e_6, listOf(R.drawable.kost_e_6, R.drawable.kost_i_6), s),
            Kost("Shervansy Kost", "Rp 1.400.000", "Jl. Yani No.102, Medan", 4.7, R.drawable.kost_e_7, listOf(R.drawable.kost_e_7, R.drawable.kost_i_7), s),
            Kost("Aivazyn Kost", "Rp 1.000.000", "Jl. Slamet No.15, Solo", 4.1, R.drawable.kost_e_8, listOf(R.drawable.kost_e_8, R.drawable.kost_i_8), s),
            Kost("L Zvan Kost", "Rp 1.400.000", "Jl. Pantai No.09, Makassar", 4.4, R.drawable.kost_e_9, listOf(R.drawable.kost_e_9, R.drawable.kost_i_9), s),
            Kost("Restvurt Kost", "Rp 2.200.000", "Jl. Dipo No.33, Semarang", 5.0, R.drawable.kost_e_10, listOf(R.drawable.kost_e_10, R.drawable.kost_i_10), s)
        )

        binding.rvKost.layoutManager = LinearLayoutManager(this)
        binding.rvKost.adapter = KostAdapter(daftarKost) { kost ->
            val intent = Intent(this, DetailKostActivity::class.java)
            intent.putExtra("EXTRA_KOST", kost)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.about_page) {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
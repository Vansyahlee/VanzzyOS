package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.utils.SafeInsets
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vanzzyos.R
import com.example.vanzzyos.adapters.KostAdapter
import com.example.vanzzyos.data.KostAssets
import com.example.vanzzyos.databinding.ActivityMainBinding
import com.example.vanzzyos.models.Kost
import com.example.vanzzyos.network.ApiClient
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var adapter: KostAdapter
    private var items: List<Kost> = emptyList()
    private var selectedCity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        setSupportActionBar(binding.toolbar)
        adapter = KostAdapter { selected ->
            startActivity(Intent(this, DetailKostActivity::class.java)
                .putExtra("EXTRA_KOST", selected))
        }
        binding.rvKost.layoutManager = LinearLayoutManager(this)
        binding.rvKost.adapter = adapter
        binding.etSearch.addTextChangedListener { filterItems() }
        binding.chipCities.setOnCheckedStateChangeListener { _, ids ->
            selectedCity = when (ids.firstOrNull()) {
                R.id.chipJakarta -> "Jakarta"
                R.id.chipBandung -> "Bandung"
                R.id.chipSurabaya -> "Surabaya"
                R.id.chipYogya -> "Yogyakarta"
                else -> ""
            }
            filterItems()
        }
        loadKosts()
    }

    private fun loadKosts() {
        binding.progress.visibility = View.VISIBLE
        worker.execute {
            val result = runCatching { ApiClient.getKosts() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                binding.progress.visibility = View.GONE
                items = result.getOrElse { KostAssets.offlineSample() }
                binding.tvConnectionHint.visibility = if (result.isFailure) View.VISIBLE else View.GONE
                if (result.isFailure) binding.tvConnectionHint.text =
                    "Belum tersambung ke server. Katalog contoh masih bisa dilihat, tetapi pemesanan sementara tidak tersedia."
                filterItems()
            }
        }
    }

    private fun filterItems() {
        if (!::adapter.isInitialized) return
        val q = binding.etSearch.text?.toString()?.trim().orEmpty()
        val filtered = items.filter {
            (selectedCity.isBlank() || it.kota.equals(selectedCity, true)) &&
                (q.isBlank() || it.nama.contains(q, true) || it.kota.contains(q, true) || it.alamat.contains(q, true))
        }
        adapter.submit(filtered)
        binding.tvCount.text = "${filtered.size} pilihan kost · harga per bulan"
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.my_orders -> { startActivity(Intent(this, MyBookingsActivity::class.java)); return true }
            R.id.profile_page -> { startActivity(Intent(this, ProfileHubActivity::class.java)); return true }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}

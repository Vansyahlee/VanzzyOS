package com.example.vanzzyos.data

import com.example.vanzzyos.R
import com.example.vanzzyos.models.Kost
import com.example.vanzzyos.utils.Money
import org.json.JSONObject

object KostAssets {
    private val cover = intArrayOf(
        R.drawable.kost_e_1, R.drawable.kost_e_2, R.drawable.kost_e_3,
        R.drawable.kost_e_4, R.drawable.kost_e_5, R.drawable.kost_e_6,
        R.drawable.kost_e_7, R.drawable.kost_e_8, R.drawable.kost_e_9, R.drawable.kost_e_10
    )
    private val interior = intArrayOf(
        R.drawable.kost_i_1, R.drawable.kost_i_2, R.drawable.kost_i_3,
        R.drawable.kost_i_4, R.drawable.kost_i_5, R.drawable.kost_i_6,
        R.drawable.kost_i_7, R.drawable.kost_i_8, R.drawable.kost_i_9, R.drawable.kost_i_10
    )
    fun fromJson(item: JSONObject): Kost {
        val id = item.getString("id")
        val position = id.drop(1).toIntOrNull()?.minus(1)?.coerceIn(0, 9) ?: 0
        val facilities = item.optJSONArray("facilities")
        val features = if (facilities == null) emptyList() else List(facilities.length()) { facilities.getString(it) }
        val price = item.getLong("price_monthly")
        return Kost(
            id = id, nama = item.getString("name"), harga = Money.rupiah(price),
            hargaBulanan = price, alamat = item.getString("address"), kota = item.getString("city"),
            rating = item.optDouble("rating", 0.0), fotoUtama = cover[position],
            daftarFoto = listOf(cover[position], interior[position]), fasilitas = features,
            kapasitas = item.optInt("capacity", 1), deskripsi = item.optString("description", "Kost nyaman."),
            sumberDemo = false
        )
    }
    fun offlineSample(): List<Kost> {
        // A preview of the existing sample listings. Checkout is disabled offline.
        val rows = listOf(
            Triple("Vanlyy Kost", "Jakarta", 1200000L),
            Triple("VanCooper Kost", "Bandung", 1100000L),
            Triple("Suzin Kost", "Yogyakarta", 950000L),
            Triple("Mostzan Kost", "Surabaya", 1500000L),
            Triple("Zevun Kost", "Bali", 850000L),
            Triple("Jayvans Kost", "Palembang", 1300000L),
            Triple("Shervansy Kost", "Medan", 1400000L),
            Triple("Aivazyn Kost", "Solo", 1000000L),
            Triple("L Zvan Kost", "Makassar", 1400000L),
            Triple("Restvurt Kost", "Semarang", 2200000L)
        )
        return rows.mapIndexed { i, row ->
            Kost(
                id = "k" + (i + 1).toString().padStart(2, '0'), nama = row.first,
                harga = Money.rupiah(row.third), hargaBulanan = row.third,
                alamat = row.second + " · Contoh alamat", kota = row.second,
                rating = listOf(4.8, 4.5, 3.8, 4.9, 3.5, 4.2, 4.7, 4.1, 4.4, 5.0)[i],
                fotoUtama = cover[i], daftarFoto = listOf(cover[i], interior[i]),
                fasilitas = listOf("WiFi", "AC", "Kamar mandi dalam", "Kasur", "Lemari", "CCTV"),
                kapasitas = 0, deskripsi = "Pratinjau properti dari proyek Android lama.", sumberDemo = true
            )
        }
    }
}

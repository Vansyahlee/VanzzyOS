package com.example.vanzzyos.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Kost(
    val id: String,
    val nama: String,
    val harga: String,
    val hargaBulanan: Long,
    val alamat: String,
    val kota: String,
    val rating: Double,
    val fotoUtama: Int,
    val daftarFoto: List<Int>,
    val fasilitas: List<String>,
    val kapasitas: Int,
    val deskripsi: String,
    val sumberDemo: Boolean = false
) : Parcelable

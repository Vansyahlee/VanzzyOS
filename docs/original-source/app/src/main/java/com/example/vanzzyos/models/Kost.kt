package com.example.vanzzyos.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Kost(
    val nama: String,
    val harga: String,
    val alamat: String,
    val rating: Double,
    val fotoUtama: Int,
    val daftarFoto: List<Int>,
    val fasilitas: List<String>
) : Parcelable
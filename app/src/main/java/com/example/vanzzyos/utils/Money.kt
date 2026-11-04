package com.example.vanzzyos.utils

import java.text.NumberFormat
import java.util.Locale

object Money {
    fun rupiah(value: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        .format(value).replace(",00", "")
}

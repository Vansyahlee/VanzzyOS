package com.example.vanzzyos.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.utils.SafeInsets
import com.example.vanzzyos.databinding.ActivityCheckoutBinding
import com.example.vanzzyos.models.Kost
import com.example.vanzzyos.network.ApiClient
import com.example.vanzzyos.network.AccountSession
import com.example.vanzzyos.utils.Money
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private val worker = Executors.newSingleThreadExecutor()
    private val calendar: Calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val reference = UUID.randomUUID().toString()
    private var kost: Kost? = null
    private var months = 1
    private var quote: ApiClient.Quote? = null
    private var generation = 0
    private var creating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        kost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_KOST", Kost::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_KOST")
        }
        val k = kost ?: run { finish(); return }
        if (k.sumberDemo) { finish(); return }
        binding.ivRoom.setImageResource(k.fotoUtama)
        binding.tvKostName.text = k.nama
        binding.tvKostAddress.text = k.alamat
        binding.btnChooseDate.setOnClickListener { openDatePicker() }
        binding.btnMinus.setOnClickListener { if (months > 1) { months--; updateSelection() } }
        binding.btnPlus.setOnClickListener { if (months < 12) { months++; updateSelection() } }
        binding.cbAgreement.setOnCheckedChangeListener { _, _ -> updateButton() }
        binding.btnConfirm.setOnClickListener { submitBooking() }
        updateSelection()
    }

    private fun selectedDate(): String = dateFormat.format(calendar.time)
    private fun updateSelection() {
        binding.tvMoveInDate.text = selectedDate()
        binding.tvMonths.text = "$months bulan"
        getQuote()
    }
    private fun openDatePicker() {
        val date = DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            updateSelection()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        date.datePicker.minDate = System.currentTimeMillis() - 60_000
        date.datePicker.maxDate = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.timeInMillis
        date.show()
    }
    private fun getQuote() {
        val id = kost?.id ?: return
        val revision = ++generation
        val date = selectedDate()
        val selectedMonths = months
        quote = null
        binding.tvQuoteAmount.text = "Menghitung total…"
        binding.tvStatus.text = ""
        updateButton()
        worker.execute {
            val result = runCatching { ApiClient.getQuote(id, date, selectedMonths) }
            runOnUiThread {
                if (isFinishing || isDestroyed || revision != generation) return@runOnUiThread
                result.onSuccess {
                    quote = it
                    binding.tvQuoteAmount.text = Money.rupiah(it.total)
                    binding.tvQuoteDescription.text = "${Money.rupiah(it.monthly)} × $selectedMonths bulan · hingga ${it.endDate}"
                    binding.tvStatus.text = if (it.availableRooms > 0)
                        "Ketersediaan simulasi: ${it.availableRooms} kamar pada periode ini."
                    else "Kamar tidak tersedia pada tanggal pilihan."
                }.onFailure {
                    binding.tvQuoteAmount.text = "Belum dapat dihitung"
                    binding.tvStatus.text = "Gagal mendapatkan harga dari server: ${it.message ?: "coba lagi"}"
                }
                updateButton()
            }
        }
    }
    private fun updateButton() {
        binding.btnConfirm.isEnabled = quote?.availableRooms?.let { it > 0 } == true &&
            binding.cbAgreement.isChecked && !creating
    }
    private fun submitBooking() {
        if (!AccountSession.isLoggedIn(this)) {
            Toast.makeText(this, "Masuk ke akun dulu untuk memesan kamar", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, AccountSettingsActivity::class.java))
            return
        }
        val k = kost ?: return
        val name = binding.etGuestName.text?.toString()?.trim().orEmpty()
        val phone = binding.etGuestPhone.text?.toString()?.trim().orEmpty()
        if (name.length < 3) { binding.etGuestName.error = "Isi nama lengkap"; return }
        if (phone.filter { it.isDigit() }.length < 9) { binding.etGuestPhone.error = "Nomor HP tidak valid"; return }
        if (quote == null || !binding.cbAgreement.isChecked) return
        creating = true
        binding.btnConfirm.text = "Menyimpan pemesanan…"
        updateButton()
        val date = selectedDate()
        val count = months
        worker.execute {
            val result = runCatching { ApiClient.createBooking(k.id, date, count, reference, name, phone, this) }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                creating = false
                binding.btnConfirm.text = "Ajukan pemesanan  →"
                result.onSuccess { booking ->
                    startActivity(Intent(this, BookingSuccessActivity::class.java)
                        .putExtra("booking_id", booking.id)
                        .putExtra("kost_name", booking.name)
                        .putExtra("move_in", booking.date)
                        .putExtra("total", booking.total))
                    finish()
                }.onFailure { error ->
                    binding.tvStatus.text = "Pemesanan gagal: ${error.message ?: "coba lagi"}"
                    Toast.makeText(this, "Pemesanan belum tersimpan", Toast.LENGTH_SHORT).show()
                }
                updateButton()
            }
        }
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}

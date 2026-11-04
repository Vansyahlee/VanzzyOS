package com.example.vanzzyos.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.databinding.ActivityAccountSettingsBinding
import com.example.vanzzyos.network.AccountApi
import com.example.vanzzyos.network.AccountSession
import com.example.vanzzyos.network.UserProfile
import com.example.vanzzyos.utils.SafeInsets
import java.util.concurrent.Executors

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingsBinding
    private val worker = Executors.newSingleThreadExecutor()
    private var register = false
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSwitchMode.setOnClickListener { register = !register; renderMode() }
        binding.btnLogin.setOnClickListener { loginOrRegister() }
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnChangePassword.setOnClickListener { changePassword() }
        binding.btnLogout.setOnClickListener { logout() }
        renderMode()
        showSession()
    }

    private fun renderMode() {
        binding.tvAuthMode.text = if (register) "Buat akun pengguna" else "Masuk ke akunmu"
        binding.registerFields.visibility = if (register) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (register) "Daftar & lanjutkan" else "Masuk"
        binding.btnSwitchMode.text = if (register) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar"
    }
    private fun showSession() {
        val loggedIn = AccountSession.isLoggedIn(this)
        binding.authSection.visibility = if (loggedIn) View.GONE else View.VISIBLE
        binding.profileSection.visibility = if (loggedIn) View.VISIBLE else View.GONE
        if (loggedIn) refreshProfile()
    }
    private fun showStatus(message: String) {
        binding.tvStatus.text = message
        if (message.isNotBlank()) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun runOperation(task: () -> Unit) {
        if (busy) return
        busy = true
        binding.tvStatus.text = "Memproses…"
        worker.execute {
            val outcome = runCatching(task)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                busy = false
                outcome.exceptionOrNull()?.let { showStatus(it.message ?: "Gagal menghubungi server") }
            }
        }
    }
    private fun loginOrRegister() {
        val email = binding.etLoginEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etLoginPassword.text?.toString().orEmpty()
        val name = binding.etRegisterName.text?.toString()?.trim().orEmpty()
        val phone = binding.etRegisterPhone.text?.toString()?.trim().orEmpty()
        if (email.isBlank() || password.isBlank() || (register && name.isBlank())) {
            showStatus("Lengkapi formulir lebih dulu"); return
        }
        runOperation {
            val profile = if (register) AccountApi.register(this, name, email, phone, password)
                else AccountApi.login(this, email, password)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    fillProfile(profile)
                    binding.authSection.visibility = View.GONE
                    binding.profileSection.visibility = View.VISIBLE
                    showStatus("Berhasil masuk sebagai ${profile.name}")
                }
            }
        }
    }
    private fun refreshProfile() {
        runOperation {
            val profile = AccountApi.me(this)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    fillProfile(profile)
                    binding.tvStatus.text = "Pengaturan akun tersinkron dengan server"
                }
            }
        }
    }
    private fun fillProfile(profile: UserProfile) {
        binding.etName.setText(profile.name)
        binding.etEmail.setText(profile.email)
        binding.etPhone.setText(profile.phone)
        binding.switchNotifications.isChecked = profile.notifications
    }
    private fun saveProfile() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        val notifications = binding.switchNotifications.isChecked
        runOperation {
            val updated = AccountApi.update(this, name, email, phone, notifications)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    fillProfile(updated)
                    showStatus("Profil tersimpan di backend")
                }
            }
        }
    }
    private fun changePassword() {
        val old = binding.etOldPassword.text?.toString().orEmpty()
        val fresh = binding.etNewPassword.text?.toString().orEmpty()
        if (old.isBlank() || fresh.length < 10) {
            showStatus("Isi sandi lama dan sandi baru minimal 10 karakter"); return
        }
        runOperation {
            AccountApi.changePassword(this, old, fresh)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    binding.etOldPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    showSession()
                    showStatus("Password diganti, silakan masuk lagi")
                }
            }
        }
    }
    private fun logout() {
        runOperation {
            AccountApi.logout(this)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    showSession()
                    showStatus("Berhasil keluar dari akun")
                }
            }
        }
    }
    override fun onDestroy() { worker.shutdownNow(); super.onDestroy() }
}

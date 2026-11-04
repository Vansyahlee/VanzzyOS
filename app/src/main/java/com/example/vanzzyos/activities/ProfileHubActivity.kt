package com.example.vanzzyos.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.databinding.ActivityProfileHubBinding
import com.example.vanzzyos.network.AccountSession
import com.example.vanzzyos.utils.SafeInsets

/** Profile menu deliberately separates business owner's public info from private user settings. */
class ProfileHubActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileHubBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileHubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.cardOwner.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }
        binding.cardAccount.setOnClickListener { startActivity(Intent(this, AccountSettingsActivity::class.java)) }
    }
    override fun onResume() {
        super.onResume()
        binding.tvAccountStatus.text = if (AccountSession.isLoggedIn(this))
            "Profil pribadi, keamanan dan preferensi akun" else "Masuk atau buat akun untuk menyimpan pengaturan"
    }
}

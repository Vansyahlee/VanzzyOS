package com.example.vanzzyos.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vanzzyos.R
import com.example.vanzzyos.databinding.ActivityAboutBinding
import com.example.vanzzyos.utils.SafeInsets

/** Owner/about page is public and NEVER edits another user's account. */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SafeInsets.apply(this, binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // If supplied by the owner, place ren_photo.jpg in res/drawable/.
        // The original vansyah.jpeg remains the safe fallback.
        val renPhoto = resources.getIdentifier("ren_photo", "drawable", packageName)
        binding.imgOwner.setImageResource(if (renPhoto != 0) renPhoto else R.drawable.vansyah)

        binding.btnEmail.setOnClickListener { open("mailto:rvansyahzulfanurjaya@gmail.com") }
        binding.btnInstagram.setOnClickListener { open("https://www.instagram.com/rvnsyhzn/") }
        binding.btnTiktok.setOnClickListener { open("https://www.tiktok.com/@vanszn05") }
    }
    private fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try { startActivity(intent) } catch (_: ActivityNotFoundException) { /* no handler installed */ }
    }
}

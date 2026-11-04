<div align="center">

# Vanzzy Stay

### Android Kost Discovery & Booking Application

**Android Kotlin + ASP.NET Core 10 Web API**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![.NET](https://img.shields.io/badge/.NET_10-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)

</div>

## Overview

Vanzzy Stay dikembangkan dari proyek **VanzzyOS** berbasis Kotlin. Aplikasi mencakup pencarian kost, detail kamar, pengajuan booking, profil publik pemilik usaha, dan **pengaturan akun masing-masing pengguna** yang tersimpan lewat backend C#.

**Project status: functional development demo.** Daftar kost, alamat, foto, harga, dan ketersediaan adalah **data contoh**. Booking berstatus `PENDING`, tanpa pembayaran sungguhan atau konfirmasi pemilik kost.

## Features

- Pencarian dan filter lokasi kost, foto, detail fasilitas, serta galeri.
- Checkout: tanggal masuk, durasi bulanan, estimasi harga dari server dan pengajuan pemesanan.
- Pemesanan memerlukan login agar riwayat hanya tampil pada akun yang membuatnya.
- Profil: **About Owner** (pemilik/pembuat aplikasi, foto, dan kontak) serta **Pengaturan Akun** (pengguna biasa).
- Pengaturan akun: daftar, masuk, ganti nama/email/nomor HP, preferensi notifikasi, ganti kata sandi, keluar.
- Data akun dan booking tersimpan **lokal di backend** dalam file JSON, termasuk setelah server dimatikan dan dijalankan lagi.
- Password menggunakan PBKDF2 dengan salt; sesi login menggunakan token acak yang di-hash saat disimpan server.

## Technology Stack

| Component | Technology |
| --- | --- |
| Android | Kotlin, Android Views/XML, Material Design |
| API | C# / ASP.NET Core 10 Minimal API |
| Local storage | JSON file (development only) |
| Network | REST API / JSON |
| Build | Android Studio & Gradle |

## Run Backend (.NET 10)

Pastikan SDK .NET 10 sudah terinstal. Di **PowerShell**, jalankan:

```powershell
cd "D:\VanzzyOS-Kotlin-DotNet\backend\VanzzyStay.Api"
dotnet --version
dotnet restore
dotnet run
```

Buka `http://127.0.0.1:8086/health` untuk memeriksa server. API akan berjalan di `127.0.0.1:8086` dan menyimpan data ke folder `backend/VanzzyStay.Api/data/` (otomatis dibuat, **tidak untuk di-upload ke GitHub**).

> Jika muncul konflik port 8086, hentikan backend Go lama dengan `Ctrl+C` sebelum menjalankan .NET.

## Run Android

1. Buka folder **root** `D:\VanzzyOS-Kotlin-DotNet` dengan Android Studio (folder yang berisi `settings.gradle.kts`).
2. Atur Gradle JDK ke **Embedded JDK 21**, install Android SDK 36 bila diminta, kemudian jalankan **Gradle Sync**.
3. Jalankan **Android Emulator**, tekan tombol **Run ▶**.
4. Android emulator memakai `http://10.0.2.2:8086` untuk menghubungi server .NET yang berjalan di laptop.
5. Masuk menu **Profil** → **Pengaturan Akun** → Daftar/Login; setelah itu buka kost dan coba ajukan pemesanan.

Jika `JAVA_HOME` tidak ditemukan ketika build melalui PowerShell, pilih JBR yang dipakai Android Studio, misalnya:

```powershell
$env:JAVA_HOME = "D:\Android\ADS Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

Sesuaikan path JBR. Peringatan PSReadLine bukan penyebab masalah Gradle.

## Foto Owner: `ren_photo.jpg`

Foto `ren_photo.jpg` **belum terdapat dalam ZIP sumber yang diterima**. Agar foto itu muncul di halaman About Owner, salin sendiri foto lu ke:

```
app/src/main/res/drawable/ren_photo.jpg
```

Nama harus persis `ren_photo.jpg` (huruf kecil dan underscore). Tidak perlu mengubah Kotlin: Android otomatis menggunakan foto itu. Bila belum ada, aplikasi menggunakan aset lama `vansyah.jpeg` sebagai fallback. **Skill dan deskripsi skill lama sudah dihilangkan** dari About Owner, sedangkan email, Instagram, TikTok tetap tersedia.

## API Accounts

| Method | Route | Keterangan |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Buat akun pengguna |
| POST | `/api/v1/auth/login` | Login dan terima token |
| GET | `/api/v1/account/me` | Baca profil akun sendiri (Bearer token) |
| PUT | `/api/v1/account/me` | Edit profil + preferensi notifikasi |
| POST | `/api/v1/account/change-password` | Ganti kata sandi dan invalidasi sesi |
| POST | `/api/v1/auth/logout` | Akhiri sesi |
| GET | `/api/v1/account/bookings` | Riwayat booking akun sendiri |

API katalog/booking lama tetap tersedia: `GET /api/v1/kost`, `GET /api/v1/kost/{id}`, `POST /api/v1/bookings/quote`, `POST /api/v1/bookings`.

**Pengaturan yang tersimpan ≠ layanan notifikasi yang sudah aktif.** Switch notifikasi saat ini menyimpan preferensi saja; pengiriman push notification belum diimplementasikan.

## Security / Batasan

- Server demo memakai HTTP lokal. **Jangan** menjalankan backend ini di internet atau memakai data/password nyata sebelum menggunakan HTTPS, database transaksional, rate limit, Android Keystore, dan mekanisme pemulihan akun.
- Token di aplikasi disimpan di `SharedPreferences` lokal untuk demo. Untuk produksi pindahkan ke penyimpanan yang dilindungi Keystore.
- Data kost belum diverifikasi; saldo dan pembayaran otomatis belum tersedia.
- Profil owner adalah informasi publik, sedangkan setiap akun hanya berhak membaca/menyimpan pengaturan dan booking miliknya sendiri.
- Untuk menguji backend otomatis dari Windows, lihat `backend/smoke-test.ps1`.

---

<div align="center">Vanzzy Stay — Personal Development Project</div>

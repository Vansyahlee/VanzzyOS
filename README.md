
<div align="center">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" width="65" alt="Kotlin" />
  &nbsp;&nbsp;&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/dotnetcore/dotnetcore-original.svg" width="65" alt=".NET" />

  # VanzzyOS

  ### Vanzzy Haven

  **Find Your Place to Stay.**

  Aplikasi Android untuk mencari kost dan penginapan, melihat detail kamar dan fasilitas, serta melakukan pemesanan melalui satu aplikasi.

  **Kotlin · Android XML · ASP.NET Core (.NET) · REST API**
</div>

---

## About

**Vanzzy Haven** adalah aplikasi pencarian dan pemesanan kost berbasis Android. Project **VanzzyOS** dikembangkan menggunakan **Kotlin** untuk aplikasi mobile dan **C# / ASP.NET Core (.NET)** untuk backend.

## Features

- Pencarian kost dan filter kota
- Detail kamar, foto, fasilitas, dan harga
- Akun dan profil pengguna
- Alur pemesanan kamar *(integrasi backend dalam pengembangan)*

## Run Project

**Backend (.NET):**

```powershell
cd backend/VanzzyStay.Api
dotnet run
```

**Android (Kotlin):** buka project di Android Studio dan jalankan melalui perangkat Android, atau build dengan:

```powershell
.\gradlew.bat :app:assembleDebug
```

Untuk pengujian backend lokal melalui HP yang tersambung USB:

```powershell
adb -d reverse tcp:8086 tcp:8086
```

## Update

**#Update NewVanzzyOS** — pengembangan VanzzyOS menjadi **Vanzzy Haven** dengan antarmuka Android yang diperbarui dan integrasi backend .NET.

---

<div align="center">
  Made by <a href="https://github.com/Vansyahlee">Vansyahlee</a>
</div>

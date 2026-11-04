
<div align="center">
  <p><strong>#Update NewVanzzyOS</strong></p>

  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" width="100" alt="Kotlin" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/dotnetcore/dotnetcore-original.svg" width="100" alt=".NET" />

  <h1>VanzzyOS</h1>

  <h3>Vanzzy Haven — Android Accommodation Booking Application</h3>

  <p>Temukan kost dan penginapan, jelajahi fasilitas kamar, dan lakukan pemesanan dengan mudah melalui aplikasi Android.</p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/.NET-512BD4?style=for-the-badge&logo=dotnet&logoColor=white" alt=".NET" />
    <img src="https://img.shields.io/badge/C%23-239120?style=for-the-badge&logo=csharp&logoColor=white" alt="C#" />
  </p>
</div>

---

## Technology Stack

| Component | Technology |
| --- | --- |
| Android | Kotlin, XML Layout |
| Backend | C#, ASP.NET Core (.NET 10) |
| API | REST API |

## Features

- Pencarian kost dan filter kota
- Detail kamar, foto, fasilitas, dan harga
- Registrasi, login, dan profil pengguna
- Pemesanan kamar *(integrasi backend dalam pengembangan)*

## Run Project

**Backend:**

```powershell
cd backend/VanzzyStay.Api
dotnet run
```

**Android:**

Buka project di Android Studio atau build melalui terminal:

```powershell
.\gradlew.bat :app:assembleDebug
```

**Koneksi backend melalui USB:**

```powershell
adb -d reverse tcp:8086 tcp:8086
```

---

<p align="center">
  Developed by <a href="https://github.com/Vansyahlee">Vansyahlee</a>
</p>

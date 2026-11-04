# Jalankan dari terminal Cursor ketika USB Debugging HP sudah diizinkan.
$ErrorActionPreference = "Stop"
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw "adb belum tersedia di PATH. Gunakan Android SDK platform-tools."
}
Write-Host "Memeriksa backend lokal..."
try {
    $health = Invoke-RestMethod "http://127.0.0.1:8086/health" -TimeoutSec 5
    Write-Host "Backend aktif:" $health.status
} catch {
    throw "Backend .NET belum berjalan di port 8086. Jalankan dotnet run di backend\VanzzyStay.Api."
}
$all = @(& adb devices)
$devices = @($all | Where-Object { $_ -match '^([^\s]+)\s+device$' -and $matches[1] -notlike 'emulator-*' } | ForEach-Object { ($_ -split '\s+')[0] })
if ($devices.Count -ne 1) {
    Write-Host ($all -join "`n")
    throw "Sambungkan tepat satu HP fisik dengan USB Debugging dan status device."
}
$serial = $devices[0]
& adb -s $serial reverse tcp:8086 tcp:8086
if ($LASTEXITCODE -ne 0) { throw "ADB reverse gagal untuk $serial" }
Write-Host "Koneksi siap ke HP: $serial"
Write-Host "Coba buka http://127.0.0.1:8086/health pada browser HP (USB tetap terpasang)."
& adb -s $serial reverse --list

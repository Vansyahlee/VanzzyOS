# Run AFTER: cd backend/VanzzyStay.Api; dotnet run
$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8086"
function PostJson($path, $data, $headers = @{}) {
    Invoke-RestMethod -Uri ($base + $path) -Method Post -Headers $headers -ContentType 'application/json' -Body ($data | ConvertTo-Json -Depth 9)
}
function PutJson($path, $data, $headers) {
    Invoke-RestMethod -Uri ($base + $path) -Method Put -Headers $headers -ContentType 'application/json' -Body ($data | ConvertTo-Json -Depth 9)
}
$health = Invoke-RestMethod "$base/health"
if ($health.status -ne 'ok') { throw 'Health check failed' }
$unique = [guid]::NewGuid().ToString('N').Substring(0, 10)
$first = PostJson '/api/v1/auth/register' @{
    display_name='Penguji Satu'; email="test1-$unique@example.test"; phone='081234567890'; password='contoh-password-123'
}
$second = PostJson '/api/v1/auth/register' @{
    display_name='Penguji Dua'; email="test2-$unique@example.test"; phone='081234567891'; password='contoh-password-321'
}
$h1 = @{ Authorization = "Bearer $($first.token)" }
$h2 = @{ Authorization = "Bearer $($second.token)" }
$updated = PutJson '/api/v1/account/me' @{
    display_name='Penguji Satu Update'; email="test1-$unique@example.test"; phone='081234567892'; notify_bookings=$false
} $h1
if ($updated.display_name -ne 'Penguji Satu Update' -or $updated.notify_bookings) { throw 'Profile not persisted' }
$accountTwo = Invoke-RestMethod -Uri "$base/api/v1/account/me" -Headers $h2
if ($accountTwo.display_name -ne 'Penguji Dua') { throw 'User isolation broken' }
$date = [datetime]::UtcNow.AddDays(3).ToString('yyyy-MM-dd')
$quote = PostJson '/api/v1/bookings/quote' @{ kost_id='k01'; move_in_date=$date; months=1 }
if ($quote.total -ne 1200000) { throw 'Quote price mismatch' }
$ref = [guid]::NewGuid().ToString()
$booking = PostJson '/api/v1/bookings' @{
    kost_id='k01'; move_in_date=$date; months=1; client_reference=$ref
    guest_name='Penguji Satu'; guest_phone='081234567892'; payment_method='pay_on_arrival'
} $h1
$list1 = Invoke-RestMethod -Uri "$base/api/v1/account/bookings" -Headers $h1
$list2 = Invoke-RestMethod -Uri "$base/api/v1/account/bookings" -Headers $h2
if ($list1.items.Count -lt 1 -or ($list2.items | Where-Object id -eq $booking.booking.id)) { throw 'Booking isolation broken' }
Write-Host 'PASS: health, registration, profile update, user isolation, quote, booking, private history' -ForegroundColor Green

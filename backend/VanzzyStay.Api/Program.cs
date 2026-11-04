using System.Security.Cryptography;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;

var builder = WebApplication.CreateBuilder(args);
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower;
    options.SerializerOptions.PropertyNameCaseInsensitive = true;
});
builder.Services.AddSingleton(new DemoStore(
    Environment.GetEnvironmentVariable("VANZZY_DATA_FILE") ?? "data/vanzzy-state.json"));

// Emulator 10.0.2.2 forwards to the host computer's localhost. Never expose
// this development HTTP server publicly or use it for real payments.
var address = Environment.GetEnvironmentVariable("VANZZY_ADDR") ?? "http://127.0.0.1:8086";
builder.WebHost.UseUrls(address);
var app = builder.Build();
app.Use(async (ctx, next) =>
{
    ctx.Response.Headers.CacheControl = "no-store";
    try { await next(); }
    catch (ApiException ex)
    {
        if (!ctx.Response.HasStarted)
        {
            ctx.Response.StatusCode = ex.Code;
            await ctx.Response.WriteAsJsonAsync(new { error = ex.Message });
        }
    }
    catch (Exception ex)
    {
        app.Logger.LogError(ex, "Unhandled API error");
        if (!ctx.Response.HasStarted)
        {
            ctx.Response.StatusCode = 500;
            await ctx.Response.WriteAsJsonAsync(new { error = "Terjadi kesalahan pada server" });
        }
    }
});

app.MapGet("/health", () => Results.Ok(new { status = "ok", service = "Vanzzy Haven ASP.NET Core API" }));
app.MapGet("/api/v1/kost", (DemoStore s, string? q, string? city, long? max_price) =>
{
    var items = s.Catalog.Where(k =>
        (string.IsNullOrWhiteSpace(q) || (k.Name + " " + k.Address + " " + k.City).Contains(q.Trim(), StringComparison.OrdinalIgnoreCase)) &&
        (string.IsNullOrWhiteSpace(city) || k.City.Contains(city.Trim(), StringComparison.OrdinalIgnoreCase)) &&
        (max_price is null or <= 0 || k.PriceMonthly <= max_price)).ToArray();
    return Results.Ok(new { items, total = items.Length });
});
app.MapGet("/api/v1/kost/{id}", (DemoStore s, string id) =>
    Results.Ok(s.FindKost(id) ?? throw new ApiException(404, "Kost tidak ditemukan")));
app.MapPost("/api/v1/bookings/quote", (DemoStore s, QuoteRequest request) => Results.Ok(s.Quote(request)));
app.MapPost("/api/v1/bookings", (DemoStore s, HttpContext ctx, BookingRequest request) =>
{
    string userId = s.RequireAccount(ctx.Request.Headers.Authorization.ToString()).Id;
    var result = s.CreateBooking(request, userId);
    return Results.Json(result, statusCode: 201);
});
app.MapGet("/api/v1/bookings/{id}", (DemoStore s, HttpContext ctx, string id) =>
    Results.Ok(s.GetBooking(id, ctx.Request.Headers["X-Booking-Token"].ToString())));

app.MapPost("/api/v1/auth/register", (DemoStore s, RegisterRequest request) =>
    Results.Json(s.Register(request), statusCode: 201));
app.MapPost("/api/v1/auth/login", (DemoStore s, LoginRequest request) => Results.Ok(s.Login(request)));
app.MapGet("/api/v1/account/me", (DemoStore s, HttpContext ctx) =>
    Results.Ok(s.RequireAccount(ctx.Request.Headers.Authorization.ToString())));
app.MapPut("/api/v1/account/me", (DemoStore s, HttpContext ctx, UpdateProfileRequest request) =>
    Results.Ok(s.UpdateProfile(s.RequireAccount(ctx.Request.Headers.Authorization.ToString()).Id, request)));
app.MapPost("/api/v1/account/change-password", (DemoStore s, HttpContext ctx, ChangePasswordRequest request) =>
{
    s.ChangePassword(s.RequireAccount(ctx.Request.Headers.Authorization.ToString()).Id, request);
    return Results.Ok(new { message = "Kata sandi diperbarui" });
});
app.MapPost("/api/v1/auth/logout", (DemoStore s, HttpContext ctx) =>
{
    s.Logout(ctx.Request.Headers.Authorization.ToString());
    return Results.Ok(new { message = "Berhasil keluar" });
});
app.MapGet("/api/v1/account/bookings", (DemoStore s, HttpContext ctx) =>
{
    var id = s.RequireAccount(ctx.Request.Headers.Authorization.ToString()).Id;
    return Results.Ok(new { items = s.BookingsOfUser(id) });
});

app.Run();

public sealed class ApiException(int code, string message) : Exception(message)
{
    public int Code { get; } = code;
}

public record Kost(string Id, string Name, string City, string Address, double Rating,
    long PriceMonthly, int Capacity, string[] Facilities, string Description);
public record QuoteRequest(string KostId, string MoveInDate, int Months);
public record Quote(string KostId, string KostName, string MoveInDate, string EndDate,
    int Months, long PriceMonthly, long Total, int AvailableRooms, string Currency);
public record BookingRequest(string KostId, string MoveInDate, int Months, string ClientReference,
    string GuestName, string GuestPhone, string PaymentMethod);
public record Booking(string Id, string ClientReference, string KostId, string KostName,
    string GuestName, string GuestPhone, string MoveInDate, string EndDate,
    int Months, long PriceMonthly, long Total, string PaymentMethod, string Status,
    DateTimeOffset CreatedAt, string? UserId);
public record RegisterRequest(string DisplayName, string Email, string Phone, string Password);
public record LoginRequest(string Email, string Password);
public record UpdateProfileRequest(string DisplayName, string Email, string Phone, bool NotifyBookings);
public record ChangePasswordRequest(string CurrentPassword, string NewPassword);
public record UserView(string Id, string DisplayName, string Email, string Phone, bool NotifyBookings);
public record LoginResult(string Token, UserView User);
public record BookingResult(Booking Booking, string AccessToken, bool Duplicate);
public sealed class AppUser
{
    public string Id { get; set; } = "";
    public string DisplayName { get; set; } = "";
    public string Email { get; set; } = "";
    public string Phone { get; set; } = "";
    public string PasswordSalt { get; set; } = "";
    public string PasswordHash { get; set; } = "";
    public bool NotifyBookings { get; set; } = true;
}
public sealed class Session
{
    public string UserId { get; set; } = "";
    public string TokenHash { get; set; } = "";
    public DateTimeOffset ExpiresAt { get; set; }
}
public sealed class BookingEntry
{
    public Booking Booking { get; set; } = default!;
    public string TokenHash { get; set; } = "";
}
public sealed class PersistedState
{
    public List<AppUser> Users { get; set; } = [];
    public List<Session> Sessions { get; set; } = [];
    public List<BookingEntry> Bookings { get; set; } = [];
}

public sealed class DemoStore
{
    private readonly object _gate = new();
    private readonly string _filename;
    private readonly JsonSerializerOptions _diskJson = new(JsonSerializerDefaults.Web) { WriteIndented = true };
    private readonly PersistedState _state;
    public IReadOnlyList<Kost> Catalog { get; } = SeedKosts();

    public DemoStore(string filename)
    {
        _filename = Path.GetFullPath(filename);
        if (File.Exists(_filename))
            _state = JsonSerializer.Deserialize<PersistedState>(File.ReadAllText(_filename), _diskJson)
                ?? new PersistedState();
        else _state = new PersistedState();
    }

    private void SaveLocked()
    {
        Directory.CreateDirectory(Path.GetDirectoryName(_filename)!);
        string temp = _filename + ".tmp";
        File.WriteAllText(temp, JsonSerializer.Serialize(_state, _diskJson));
        File.Move(temp, _filename, true);
    }

    private static IReadOnlyList<Kost> SeedKosts()
    {
        var facilities = new[] { "WiFi", "AC", "Kamar mandi dalam", "Kasur", "Lemari", "CCTV" };
        return new List<Kost>
        {
            new("k01", "Vanlyy Kost", "Jakarta", "Jl. Tebet No.10, Jakarta Selatan", 4.8, 1200000, 3, facilities, "Kost nyaman dekat pusat aktivitas kota, dengan kamar furnished dan akses mudah."),
            new("k02", "VanCooper Kost", "Bandung", "Jl. Asia No.45, Bandung", 4.5, 1100000, 4, facilities, "Tempat tinggal praktis di pusat kota Bandung, cocok untuk mahasiswa dan pekerja."),
            new("k03", "Suzin Kost", "Yogyakarta", "Jl. Malioboro No.12, Yogyakarta", 3.8, 950000, 2, facilities, "Hunian dengan akses strategis dan fasilitas dasar yang nyaman."),
            new("k04", "Mostzan Kost", "Surabaya", "Jl. Basuki No.05, Surabaya", 4.9, 1500000, 3, facilities, "Kamar modern dengan fasilitas lengkap untuk aktivitas harian."),
            new("k05", "Zevun Kost", "Bali", "Jl. Legian No.88, Bali", 3.5, 850000, 2, facilities, "Pilihan kost terjangkau dengan suasana tropis dan akses area wisata."),
            new("k06", "Jayvans Kost", "Palembang", "Jl. Sudirman No.21, Palembang", 4.2, 1300000, 2, facilities, "Kost nyaman dengan fasilitas esensial dan lokasi dekat pusat kota."),
            new("k07", "Shervansy Kost", "Medan", "Jl. Yani No.102, Medan", 4.7, 1400000, 3, facilities, "Kost dengan kamar tertata dan lingkungan yang tenang."),
            new("k08", "Aivazyn Kost", "Solo", "Jl. Slamet No.15, Solo", 4.1, 1000000, 4, facilities, "Hunian sederhana dan ramah budget untuk tinggal bulanan."),
            new("k09", "L Zvan Kost", "Makassar", "Jl. Pantai No.09, Makassar", 4.4, 1400000, 3, facilities, "Kost dengan interior yang nyaman dan fasilitas pendukung."),
            new("k10", "Restvurt Kost", "Semarang", "Jl. Dipo No.33, Semarang", 5.0, 2200000, 2, facilities, "Kost premium dengan ruang lebih lega dan fasilitas lengkap.")
        };
    }
    public Kost? FindKost(string id) => Catalog.FirstOrDefault(k => k.Id == id);
    private static DateOnly ParseMoveIn(string value)
    {
        if (!DateOnly.TryParseExact(value, "yyyy-MM-dd", System.Globalization.CultureInfo.InvariantCulture,
            System.Globalization.DateTimeStyles.None, out var date))
            throw new ApiException(400, "Tanggal harus berformat YYYY-MM-DD");
        var today = DateOnly.FromDateTime(DateTime.UtcNow);
        if (date < today || date > today.AddYears(1))
            throw new ApiException(400, "Tanggal masuk harus hari ini sampai satu tahun ke depan");
        return date;
    }
    private Quote QuoteLocked(QuoteRequest req)
    {
        var k = FindKost(req.KostId) ?? throw new ApiException(404, "Kost tidak ditemukan");
        if (req.Months is < 1 or > 12) throw new ApiException(400, "Durasi harus 1 sampai 12 bulan");
        var start = ParseMoveIn(req.MoveInDate);
        var end = start.AddMonths(req.Months);
        int reserved = _state.Bookings.Count(e => e.Booking.KostId == k.Id && e.Booking.Status != "CANCELLED" &&
            DateOnly.Parse(e.Booking.MoveInDate) < end && start < DateOnly.Parse(e.Booking.EndDate));
        return new Quote(k.Id, k.Name, start.ToString("yyyy-MM-dd"), end.ToString("yyyy-MM-dd"),
            req.Months, k.PriceMonthly, checked(k.PriceMonthly * req.Months), Math.Max(0, k.Capacity - reserved), "IDR");
    }
    public Quote Quote(QuoteRequest request) { lock (_gate) return QuoteLocked(request); }
    public BookingResult CreateBooking(BookingRequest req, string? userId)
    {
        if (req.GuestName?.Trim().Length is < 3 or > 80 || string.IsNullOrWhiteSpace(req.GuestName))
            throw new ApiException(400, "Nama penyewa harus 3–80 karakter");
        if (!Regex.IsMatch(req.GuestPhone ?? "", @"^\+?[0-9\- ]{9,18}$"))
            throw new ApiException(400, "Nomor telepon tidak valid");
        if (req.PaymentMethod != "pay_on_arrival")
            throw new ApiException(400, "Pembayaran online belum tersedia");
        if (req.ClientReference?.Length is < 10 or > 80 || string.IsNullOrWhiteSpace(req.ClientReference))
            throw new ApiException(400, "Referensi permintaan tidak valid");
        lock (_gate)
        {
            if (_state.Bookings.Any(b => b.Booking.ClientReference == req.ClientReference))
                throw new ApiException(409, "Permintaan ini sudah diproses; periksa riwayat pemesanan");
            var quote = QuoteLocked(new QuoteRequest(req.KostId, req.MoveInDate, req.Months));
            if (quote.AvailableRooms <= 0) throw new ApiException(409, "Kamar tidak tersedia pada periode ini");
            string token = RandomHex(32);
            var booking = new Booking("VNZ-" + RandomHex(7).ToUpperInvariant(), req.ClientReference,
                quote.KostId, quote.KostName, req.GuestName.Trim(), req.GuestPhone.Trim(),
                quote.MoveInDate, quote.EndDate, quote.Months, quote.PriceMonthly, quote.Total,
                req.PaymentMethod, "PENDING", DateTimeOffset.UtcNow, userId);
            var entry = new BookingEntry { Booking = booking, TokenHash = HashToken(token) };
            _state.Bookings.Add(entry);
            try { SaveLocked(); } catch { _state.Bookings.Remove(entry); throw; }
            return new BookingResult(booking, token, false);
        }
    }
    public Booking GetBooking(string id, string bookingToken)
    {
        if (bookingToken.Length < 32) throw new ApiException(401, "Token booking dibutuhkan");
        lock (_gate)
            return _state.Bookings.FirstOrDefault(b => b.Booking.Id == id &&
                CryptographicOperations.FixedTimeEquals(Convert.FromHexString(b.TokenHash),
                    SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(bookingToken))))?.Booking
                    ?? throw new ApiException(404, "Pemesanan tidak ditemukan");
    }
    public Booking[] BookingsOfUser(string userId)
    {
        lock (_gate) return _state.Bookings.Where(b => b.Booking.UserId == userId)
            .Select(b => b.Booking).OrderByDescending(b => b.CreatedAt).ToArray();
    }

    private static string RandomHex(int bytes) => Convert.ToHexString(RandomNumberGenerator.GetBytes(bytes)).ToLowerInvariant();
    private static string HashToken(string token) => Convert.ToHexString(
        SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(token)));
    private static (string salt, string hash) PasswordParts(string password, string? salt = null)
    {
        salt ??= RandomHex(16);
        var hash = Rfc2898DeriveBytes.Pbkdf2(password, Convert.FromHexString(salt),
            210_000, HashAlgorithmName.SHA256, 32);
        return (salt, Convert.ToHexString(hash));
    }
    private static bool VerifyPassword(AppUser user, string password)
    {
        var (_, hash) = PasswordParts(password, user.PasswordSalt);
        return CryptographicOperations.FixedTimeEquals(Convert.FromHexString(hash), Convert.FromHexString(user.PasswordHash));
    }
    private static string NormalizeEmail(string? email)
    {
        string result = email?.Trim().ToLowerInvariant() ?? "";
        if (result.Length > 160 || !Regex.IsMatch(result, @"^[^\s@]+@[^\s@]+\.[^\s@]+$"))
            throw new ApiException(400, "Email tidak valid");
        return result;
    }
    private static void ValidateProfile(string? name, string? phone)
    {
        if (string.IsNullOrWhiteSpace(name) || name.Trim().Length is < 2 or > 80)
            throw new ApiException(400, "Nama harus 2–80 karakter");
        if (phone is not null && phone.Length > 0 && !Regex.IsMatch(phone, @"^\+?[0-9\- ]{9,18}$"))
            throw new ApiException(400, "Nomor telepon tidak valid");
    }
    private static void ValidatePassword(string? password)
    {
        if (password is null || password.Length < 10 || password.Length > 128)
            throw new ApiException(400, "Kata sandi harus 10–128 karakter");
    }
    private UserView Public(AppUser user) => new(user.Id, user.DisplayName, user.Email, user.Phone, user.NotifyBookings);
    private LoginResult IssueTokenLocked(AppUser user)
    {
        string token = RandomHex(32);
        _state.Sessions.RemoveAll(s => s.UserId == user.Id && s.ExpiresAt < DateTimeOffset.UtcNow);
        _state.Sessions.Add(new Session { UserId = user.Id, TokenHash = HashToken(token), ExpiresAt = DateTimeOffset.UtcNow.AddDays(14) });
        return new LoginResult(token, Public(user));
    }
    public LoginResult Register(RegisterRequest req)
    {
        var email = NormalizeEmail(req.Email);
        ValidateProfile(req.DisplayName, req.Phone);
        ValidatePassword(req.Password);
        lock (_gate)
        {
            if (_state.Users.Any(u => u.Email == email)) throw new ApiException(409, "Email telah terdaftar");
            var (salt, hash) = PasswordParts(req.Password);
            var user = new AppUser { Id = Guid.NewGuid().ToString("N"), DisplayName = req.DisplayName.Trim(),
                Email = email, Phone = req.Phone?.Trim() ?? "", PasswordSalt = salt, PasswordHash = hash, NotifyBookings = true };
            _state.Users.Add(user);
            var result = IssueTokenLocked(user);
            try { SaveLocked(); } catch { _state.Users.Remove(user); _state.Sessions.RemoveAll(s => s.UserId == user.Id); throw; }
            return result;
        }
    }
    public LoginResult Login(LoginRequest req)
    {
        var email = NormalizeEmail(req.Email);
        lock (_gate)
        {
            var user = _state.Users.FirstOrDefault(u => u.Email == email);
            if (user == null || !VerifyPassword(user, req.Password ?? ""))
                throw new ApiException(401, "Email atau kata sandi salah");
            var result = IssueTokenLocked(user);
            SaveLocked();
            return result;
        }
    }
    private static string ReadToken(string header)
    {
        return header.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase) ? header[7..].Trim() : "";
    }
    public UserView? TryAccount(string header)
    {
        var token = ReadToken(header);
        if (token.Length < 32) return null;
        var hashed = HashToken(token);
        lock (_gate)
        {
            var session = _state.Sessions.FirstOrDefault(s => s.TokenHash == hashed && s.ExpiresAt > DateTimeOffset.UtcNow);
            var user = session == null ? null : _state.Users.FirstOrDefault(u => u.Id == session.UserId);
            return user == null ? null : Public(user);
        }
    }
    public UserView RequireAccount(string header) => TryAccount(header)
        ?? throw new ApiException(401, "Silakan login kembali");
    public UserView UpdateProfile(string userId, UpdateProfileRequest req)
    {
        var email = NormalizeEmail(req.Email);
        ValidateProfile(req.DisplayName, req.Phone);
        lock (_gate)
        {
            var user = _state.Users.First(u => u.Id == userId);
            if (_state.Users.Any(u => u.Email == email && u.Id != userId))
                throw new ApiException(409, "Email sudah digunakan akun lain");
            var oldName = user.DisplayName; var oldEmail = user.Email;
            var oldPhone = user.Phone; var oldNotify = user.NotifyBookings;
            user.DisplayName = req.DisplayName.Trim(); user.Email = email;
            user.Phone = req.Phone?.Trim() ?? ""; user.NotifyBookings = req.NotifyBookings;
            try { SaveLocked(); } catch
            {
                user.DisplayName = oldName; user.Email = oldEmail;
                user.Phone = oldPhone; user.NotifyBookings = oldNotify; throw;
            }
            return Public(user);
        }
    }
    public void ChangePassword(string userId, ChangePasswordRequest req)
    {
        ValidatePassword(req.NewPassword);
        lock (_gate)
        {
            var user = _state.Users.First(u => u.Id == userId);
            if (!VerifyPassword(user, req.CurrentPassword ?? ""))
                throw new ApiException(403, "Kata sandi saat ini salah");
            var (salt, hash) = PasswordParts(req.NewPassword);
            user.PasswordSalt = salt; user.PasswordHash = hash;
            // Invalidate all other sessions after a password change.
            _state.Sessions.RemoveAll(s => s.UserId == userId);
            SaveLocked();
        }
    }
    public void Logout(string header)
    {
        var token = ReadToken(header);
        if (token.Length == 0) return;
        lock (_gate)
        {
            _state.Sessions.RemoveAll(s => s.TokenHash == HashToken(token));
            SaveLocked();
        }
    }
}

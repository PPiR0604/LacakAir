# Panduan Lengkap Setup Firebase untuk LacakAir

## 📱 Langkah 1: Membuat Project Firebase

1. Buka browser dan kunjungi: **https://console.firebase.google.com/**
2. Login dengan akun Google Anda
3. Klik tombol **"Add project"** atau **"Tambahkan project"**
4. Masukkan nama project: **LacakAir** (atau nama lain yang Anda inginkan)
5. Klik **Continue**
6. Matikan Google Analytics jika tidak diperlukan (opsional)
7. Klik **Create project**
8. Tunggu beberapa saat sampai project selesai dibuat
9. Klik **Continue** untuk masuk ke dashboard project

---

## 📱 Langkah 2: Menambahkan Android App ke Firebase

1. Di halaman Firebase Console, cari ikon **Android** (robot hijau)
2. Klik ikon Android tersebut
3. Isi form dengan data berikut:
   - **Android package name**: `com.example.lacakair` (HARUS SAMA persis)
   - **App nickname**: LacakAir (opsional)
   - **Debug signing certificate SHA-1**: (kosongkan dulu, opsional)
4. Klik **Register app**
5. Download file **google-services.json**
6. **PENTING**: Copy file `google-services.json` yang baru didownload ke folder:
   ```
   C:\Users\user\AndroidStudioProjects\LacakAir\app\
   ```
   (Replace file yang sudah ada di sana)
7. Klik **Next** beberapa kali sampai selesai
8. Klik **Continue to console**

---

## 🔐 Langkah 3: Mengaktifkan Email/Password Authentication

### Ini yang Anda tanyakan! Ikuti langkah ini dengan teliti:

1. **Di sidebar kiri Firebase Console**, cari dan klik menu **"Build"** atau **"Buat"**
2. Klik **"Authentication"**
3. Jika ini pertama kali, akan muncul halaman welcome. Klik tombol **"Get started"**
4. Setelah masuk ke halaman Authentication, klik tab **"Sign-in method"** di bagian atas
5. Anda akan melihat daftar provider (Google, Facebook, Email/Password, dll)
6. **Cari dan klik pada baris "Email/Password"** (biasanya di paling atas)
7. Akan muncul dialog popup
8. **Aktifkan toggle/switch "Enable"** yang pertama (Email/Password)
   - Ada 2 switch: yang atas untuk Email/Password, yang bawah untuk Email link (passwordless)
   - **Aktifkan hanya yang atas saja**
9. Klik tombol **"Save"**
10. Sekarang status Email/Password akan berubah menjadi **"Enabled"** dengan tanda centang hijau

✅ **Authentication sudah aktif!**

---

## 🗄️ Langkah 4: Membuat Firestore Database

1. **Di sidebar kiri**, tetap di bawah menu **"Build"**
2. Klik **"Firestore Database"**
3. Klik tombol **"Create database"**
4. Pilih **"Start in test mode"** (untuk development)
   - Test mode memungkinkan read/write tanpa authentication (bagus untuk testing)
   - Nanti bisa diubah ke production mode
5. Klik **"Next"**
6. Pilih lokasi server terdekat, misalnya:
   - **asia-southeast1** (Singapore)
   - **asia-southeast2** (Jakarta)
7. Klik **"Enable"**
8. Tunggu beberapa menit sampai database selesai dibuat

✅ **Firestore Database sudah siap!**

---

## 📸 Langkah 5: (Opsional) Setup Firebase Storage

Jika nanti ingin fitur upload foto:

1. **Di sidebar kiri**, tetap di menu **"Build"**
2. Klik **"Storage"**
3. Klik **"Get started"**
4. Pilih **"Start in test mode"**
5. Klik **"Next"**
6. Pilih lokasi server yang sama dengan Firestore
7. Klik **"Done"**

---

## 🧪 Langkah 6: Menambahkan Data Dummy untuk Testing

Supaya ada postingan yang muncul di aplikasi:

1. Buka **"Firestore Database"** dari sidebar
2. Klik tombol **"Start collection"**
3. Masukkan Collection ID: **posts**
4. Klik **"Next"**
5. Biarkan Document ID auto-generate, atau ketik: **post1**
6. Tambahkan field satu per satu:

   | Field name | Field type | Field value |
   |------------|-----------|-------------|
   | userId | string | test123 |
   | userName | string | John Doe |
   | imageUrl | string | https://picsum.photos/500 |
   | caption | string | Selamat datang di LacakAir! 🌊 |
   | likes | array | (biarkan kosong/empty array) |
   | timestamp | number | 1701234567890 |

7. Klik **"Save"**
8. Ulangi langkah 2-7 untuk membuat beberapa postingan lagi dengan data berbeda

**Contoh data postingan lain:**
- imageUrl: https://picsum.photos/500?random=1
- imageUrl: https://picsum.photos/500?random=2
- imageUrl: https://picsum.photos/500?random=3

---

## ✅ Verifikasi Setup

### Cek Authentication:
1. Buka **Authentication** > **Users** tab
2. Setelah Anda register di aplikasi, user baru akan muncul di sini

### Cek Firestore:
1. Buka **Firestore Database**
2. Anda harus melihat collection **"posts"** dengan data yang tadi dibuat
3. Setelah register, collection **"users"** juga akan otomatis terbuat

---

## 🚀 Build & Run Aplikasi

Setelah semua setup Firebase selesai:

1. **Pastikan file `google-services.json` sudah ada di folder `app/`**
2. Buka Android Studio
3. Klik **File** > **Sync Project with Gradle Files**
4. Tunggu sampai sync selesai (pertama kali bisa lama karena download dependencies)
5. Klik tombol **Run** (▶️) atau tekan **Shift + F10**
6. Pilih emulator atau device fisik
7. Tunggu aplikasi ter-install dan terbuka

---

## 📱 Testing Aplikasi

### Test Register:
1. Di aplikasi, klik **"Belum punya akun? Daftar di sini"**
2. Isi form:
   - Nama: John Doe
   - Email: john@example.com
   - Password: 123456
   - Konfirmasi Password: 123456
3. Klik **"Daftar"**
4. Jika berhasil, akan otomatis masuk ke halaman Home

### Test Login:
1. Logout dari aplikasi (klik ikon logout di kanan atas)
2. Login dengan email dan password yang tadi didaftarkan
3. Klik **"Login"**

### Test Feed:
1. Setelah login, Anda akan melihat postingan yang tadi dibuat di Firestore
2. Coba klik tombol ❤️ untuk like/unlike
3. Counter "suka" akan bertambah/berkurang

---

## 🔧 Troubleshooting

### ❌ Error: "google-services.json not found"
**Solusi:**
- Pastikan file `google-services.json` ada di: `app/google-services.json`
- Bukan di root project, tapi di dalam folder `app`
- Sync Gradle lagi

### ❌ Error: "Default FirebaseApp is not initialized"
**Solusi:**
- File `google-services.json` salah tempat atau tidak ter-detect
- Clean project: **Build** > **Clean Project**
- Rebuild: **Build** > **Rebuild Project**

### ❌ Error saat Login: "There is no user record"
**Solusi:**
- User belum terdaftar, register dulu
- Atau cek di Firebase Console > Authentication > Users, apakah user ada

### ❌ Error: "PERMISSION_DENIED"
**Solusi:**
- Firestore rules terlalu ketat
- Ubah rules di Firestore > Rules tab:
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if true; // Test mode - jangan untuk production!
      }
    }
  }
  ```

### ❌ Postingan tidak muncul
**Solusi:**
- Pastikan sudah login
- Tambahkan data dummy manual di Firestore Console
- Cek internet connection
- Lihat Logcat di Android Studio untuk error message

### ❌ Like tidak berfungsi
**Solusi:**
- Pastikan sudah login (butuh currentUser.uid)
- Cek Firestore rules mengizinkan write
- Lihat Logcat untuk error

---

## 📊 Monitoring di Firebase Console

### Lihat Users yang Register:
**Authentication** > **Users** tab
- Semua user yang register akan muncul di sini
- Bisa delete, disable, atau reset password

### Lihat Data Real-time:
**Firestore Database**
- Klik collection "posts" atau "users" untuk lihat data
- Data akan update real-time saat ada perubahan dari aplikasi
- Bisa edit/delete manual di sini

### Lihat Error Logs:
**Crashlytics** (jika sudah disetup)
- Atau lihat di Android Studio Logcat

---

## 🎨 Customization

### Ubah Warna Tema:
Buka file: `Color.kt`
```kotlin
val PrimaryBlue = Color(0xFF378fdf) // Ubah kode warna di sini
```

### Ubah Nama App:
Buka file: `app/src/main/res/values/strings.xml`
```xml
<string name="app_name">LacakAir</string>
```

---

## 📚 Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

## 🆘 Butuh Bantuan?

Jika masih ada error atau pertanyaan:
1. Cek Logcat di Android Studio (tab paling bawah)
2. Screenshot error message
3. Cek Firebase Console untuk melihat apakah ada error di sana

Selamat mencoba! 🚀


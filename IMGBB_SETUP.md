# 📸 Upload Gambar Gratis dengan ImgBB

## 🎉 Perubahan dari Firebase Storage ke ImgBB

Saya sudah mengubah sistem upload dari Firebase Storage ke **ImgBB** (gratis unlimited!).

## ✅ Keuntungan ImgBB:

1. **100% Gratis** - Tidak ada biaya sama sekali
2. **Unlimited Upload** - Tidak ada batas jumlah foto
3. **Tidak Perlu Kartu Kredit** - Cukup daftar dengan email
4. **API Sederhana** - Mudah digunakan
5. **CDN Global** - Loading cepat di seluruh dunia
6. **Tidak Perlu Setup Firebase Storage**

## 🔑 Cara Mendapatkan API Key ImgBB (GRATIS):

### 1. Daftar Akun ImgBB
1. Buka: **https://imgbb.com/**
2. Klik **Sign Up** di pojok kanan atas
3. Daftar dengan email Anda (atau gunakan Google/Facebook)
4. Verifikasi email jika diminta

### 2. Dapatkan API Key
1. Setelah login, buka: **https://api.imgbb.com/**
2. Klik **"Get API Key"**
3. Isi nama aplikasi: **LacakAir**
4. Klik **"Get API Key"**
5. **Copy API Key** yang muncul (seperti: `a1b2c3d4e5f6g7h8i9j0`)

### 3. Masukkan API Key ke Aplikasi
1. Buka file: `ImageUploader.kt`
2. Cari baris:
   ```kotlin
   private const val IMGBB_API_KEY = "b3b6e1e8c9c8e8f8e8e8e8e8e8e8e8e8"
   ```
3. **Ganti** dengan API key Anda:
   ```kotlin
   private const val IMGBB_API_KEY = "API_KEY_ANDA_DISINI"
   ```
4. **Save** file

## 🚀 Cara Kerja:

1. User ambil foto atau pilih dari galeri
2. Aplikasi kompres foto (max 1024px untuk efisiensi)
3. Convert foto ke Base64
4. Upload ke ImgBB via API
5. Dapat URL foto yang bisa diakses public
6. Simpan URL di Firestore

## 📱 Yang Sudah Saya Update:

### ✅ File Baru:
- `ImageUploader.kt` - Handler upload ke ImgBB

### ✅ File Diupdate:
- `PostViewModel.kt` - Menggunakan ImgBB instead of Firebase Storage
- `CreatePostScreen.kt` - Menampilkan status upload
- `NavGraph.kt` - Pass context ke uploader
- `build.gradle.kts` - Hapus dependency Firebase Storage

## 🎯 Testing:

1. **Daftar dan dapatkan API Key** dari ImgBB
2. **Masukkan API Key** ke `ImageUploader.kt`
3. **Sync Gradle**
4. **Run aplikasi**
5. **Klik tombol + di Home**
6. **Ambil foto atau pilih dari galeri**
7. **Tulis caption**
8. **Klik Posting**
9. **Tunggu upload selesai** (biasanya 2-5 detik)
10. **Foto akan muncul di feed!**

## 🔧 Troubleshooting:

### ❌ Error: "Upload failed"
**Penyebab:** API key salah atau belum diganti
**Solusi:** Pastikan API key sudah benar dan valid

### ❌ Error: "Network error"
**Penyebab:** Tidak ada koneksi internet
**Solusi:** Pastikan device terhubung ke internet

### ❌ Upload lambat
**Penyebab:** Koneksi internet lemah atau foto terlalu besar
**Solusi:** Kode sudah otomatis kompres foto ke max 1024px

## 💡 Tips:

- **API Key gratis** punya limit 5000 request/bulan (cukup untuk development)
- Jika butuh lebih, bisa buat akun baru atau upgrade (tetap murah)
- Foto yang diupload **tidak bisa dihapus** via API (fitur premium)
- Tapi untuk social media app, ini tidak masalah

## 🆚 Perbandingan:

| Fitur | Firebase Storage | ImgBB |
|-------|-----------------|-------|
| Biaya | Bayar setelah 5GB | **Gratis unlimited** |
| Setup | Perlu config Firebase | **Daftar 2 menit** |
| Kartu Kredit | Diperlukan | **Tidak perlu** |
| API | Complex | **Simple** |
| CDN | Ya | **Ya** |

## 🎨 Alternatif Lain (Jika Butuh):

### 1. **Imgur** (Juga Gratis)
- Limit: 12500 upload/hari
- API: https://api.imgur.com/

### 2. **Cloudinary** (Free Tier)
- 25GB storage gratis
- 25GB bandwidth/bulan
- Lebih profesional

### 3. **Supabase Storage** (Open Source Firebase)
- 1GB gratis
- Self-hosted option

Tapi **ImgBB sudah cukup** untuk aplikasi ini! 🚀

## ✨ Selesai!

Sekarang aplikasi Anda bisa upload foto **100% gratis** tanpa biaya Firebase Storage!

**Next Steps:**
1. Daftar ImgBB
2. Dapatkan API Key
3. Masukkan ke kode
4. Test upload foto
5. Enjoy! 🎉


# LacakAir

Aplikasi mobile untuk melacak dan membagikan informasi ketersediaan air bersih berbasis Android.

## Tech Stack
- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - UI framework
- **Firebase** - Authentication & Database
- **Google Maps** - Integrasi peta lokasi
- **MVVM Architecture** - Pattern arsitektur

## Fitur Utama
- 🔐 Login & Register dengan Firebase Auth
- 📍 Peta interaktif untuk melihat lokasi sumber air
- 📝 Posting & lihat informasi ketersediaan air
- 👤 Profil pengguna
- 🏠 Home feed dengan postingan terbaru

## Cara Install
1. Clone repository ini
```bash
git clone https://github.com/PPiR0604/LacakAir.git
```

2. Buka project di Android Studio

3. Setup Firebase:
   - Download `google-services.json` dari Firebase Console
   - Taruh di folder `app/`

4. Sync Gradle dan Run

## Struktur Project
```
app/
├── auth/          # Authentication logic
├── data/          # Models & repositories
├── ui/
│   ├── screens/   # UI screens
│   └── navigation/ # Navigation setup
└── utils/         # Helper functions
```

## Requirements
- Android Studio Narwhal+
- Minimum SDK 24
- Koneksi internet (Firebase)
- Google Maps API Key

## Catatan
Project tugas akhir semester, masih dalam tahap development.

---
Made with ☕ by PPiR0604

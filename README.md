# Quick Identify Equipment — Android 5.4.1 Offline Final

**Quick Identify Equipment** adalah aplikasi Android offline-first untuk identifikasi equipment dan akses database lubrication Processing Plant.

## Identitas build

- App name: `Quick Identify Equipment`
- Application ID: `com.wahyusetiyonyoto.quickidentifyequipment`
- Version: `5.4.1` (`versionCode 10`)
- Min SDK: 24
- Target/Compile SDK: 35
- Launcher icon: logo gear + oil-drop yang sama dengan logo di HTML aplikasi

## Offline runtime

Versi 5.4.1 dirancang agar fungsi utama tidak membutuhkan koneksi internet saat digunakan:

- UI HTML disimpan di APK sebagai `index.html.gz.b64.*`
- database awal disimpan di APK sebagai `initial-state.js.gz.b64.*`
- 324 equipment QR bawaan disimpan lokal pada `bundled-qr.js.gz.b64.*`
- decoder `jsQR` dibundel ke APK saat build
- generator QR tambahan `qie-qrcode-local.js` dibundel ke APK
- logo aplikasi disimpan lokal
- WebView tidak meminta permission `INTERNET`
- CSP HTML membatasi `connect-src` hanya ke `blob:` dan `data:` untuk kebutuhan export lokal
- fallback runtime ke `cdn.jsdelivr.net`, `cdnjs.cloudflare.com`, dan `quickchart.io` sudah dihapus
- localStorage tetap digunakan untuk perubahan data di perangkat
- camera/gallery, export CSV/PNG, dan print tetap melalui bridge Android

> Build GitHub memerlukan internet untuk mengambil dependency Gradle dan mengambil source `jsQR` dari commit yang dipin. Setelah APK selesai dibuat, runtime aplikasi tidak membutuhkan internet.

## QR offline

`jsQR` dipin ke commit:

`8e6a036beafa7053dd44b1b76ac578d22b1b3311`

Workflow memverifikasi Git blob SHA sebelum library dimasukkan ke APK:

`99ea9df26907009e5553233ffe03c529c1521739`

QR equipment bawaan tetap memakai bundle PNG lokal. Equipment baru yang ditambahkan pengguna dapat menghasilkan QR secara lokal menggunakan `qie-qrcode-local.js`.

## Lubricant Database Processing Plant 2026

Source authority: `Lubricant Database Processing Plant_2026-1.pdf`.

Database seed/migration mempertahankan hasil audit 2026:
- 324 equipment
- 892 lubrication records
- 54 lubricant master records
- 6 koreksi numeric capacity dari sumber
- 1 koreksi pemisahan capacity-unit/working-hours
- 31 equipment tambahan
- 67 lubrication rows tambahan

Kapasitas yang kosong di sumber tetap kosong. Tidak ada kapasitas yang diisi dengan perkiraan.

## Security / storage

- `android:allowBackup="false"`
- `android:fullBackupContent="false"`
- `android:usesCleartextTraffic="false"`
- permission `INTERNET` tidak digunakan
- file access WebView tetap dimatikan
- mixed content diblokir
- WebView debugging hanya aktif pada debug build

## GitHub Actions

Workflow `.github/workflows/build-apk.yml` melakukan:

1. checkout source
2. setup Java 17 + Gradle 8.9
3. mengambil `jsQR` dari commit yang dipin dan memverifikasi hash
4. memverifikasi kebijakan offline source
5. menjalankan `lintDebug` dan `assembleDebug`
6. memverifikasi package, version, signature, offline assets, dan tidak adanya permission INTERNET
7. mengunggah artifact `Quick-Identify-Equipment-V5.4.1-offline-debug`
8. bila release signing secrets tersedia, juga membuat `Quick-Identify-Equipment-V5.4.1-offline-release`

Untuk release update yang konsisten, gunakan secrets:

- `QIE_KEYSTORE_BASE64`
- `QIE_KEYSTORE_PASSWORD`
- `QIE_KEY_ALIAS`
- `QIE_KEY_PASSWORD`

Jangan commit production keystore atau password ke repository public.

## Build lokal

Sebelum build lokal:

```bash
./scripts/fetch-jsqr.sh jsQR.js
gradle --no-daemon lintDebug assembleDebug
```

## Catatan upgrade

Package final adalah `com.wahyusetiyonyoto.quickidentifyequipment`. Build lama dengan package `com.wahyusetiyonyoto.equipmentidentify` dianggap aplikasi berbeda. Data localStorage dari package lama tidak otomatis berpindah; export/import diperlukan bila data perangkat lama perlu dipertahankan.

Lihat `THIRD_PARTY_NOTICES.md` untuk attribution library QR.

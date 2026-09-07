# Quick Identify Equipment V5.4.1 — Offline Final Patch

Patch ini melengkapi V5.4 yang sudah ada di GitHub agar runtime aplikasi tidak bergantung internet.

## Yang diperbaiki

1. Menghapus permission `android.permission.INTERNET`.
2. Menambahkan Content Security Policy dengan `connect-src 'none'`.
3. Menghapus fallback runtime ke:
   - `cdn.jsdelivr.net`
   - `cdnjs.cloudflare.com`
   - `quickchart.io`
4. Menambahkan decoder QR `jsQR` sebagai asset lokal di APK.
5. Menambahkan generator QR lokal `qie-qrcode-local.js` untuk equipment baru.
6. Memastikan 324 QR equipment bawaan tetap dibundel lokal.
7. Memperbaiki project name dari V5.3 menjadi `Quick-Identify-Equipment-V5.4.1`.
8. Menaikkan version menjadi `5.4.1` (`versionCode 10`).
9. Menambahkan workflow check untuk:
   - source offline policy
   - Android Lint
   - package/version
   - APK signature
   - keberadaan asset offline
   - tidak adanya permission INTERNET
10. Mempertahankan database 2026 yang sudah tervalidasi:
    - 324 equipment
    - 892 lubrication records
    - 54 lubricant master

## File yang perlu di-upload/replace ke repository

Upload dengan struktur folder yang sama:

- `.github/workflows/build-apk.yml`
- `AndroidManifest.xml`
- `MainActivity.java`
- `build.gradle.kts`
- `settings.gradle.kts`
- `index.html`
- `index.html.gz.b64.001`
- `index.html.gz.b64.002`
- `qie-qrcode-local.js`
- `scripts/fetch-jsqr.sh`
- `README.md`
- `THIRD_PARTY_NOTICES.md`

`initial-state.js`, `initial-state.js.gz.b64.*`, `bundled-qr.js.gz.b64.*`, `launcher_logo.webp`, dan `ic_launcher.xml` tidak perlu diubah karena versi GitHub sekarang sudah benar.

## Bagaimana jsQR menjadi offline

`jsQR` tidak dimuat dari internet saat aplikasi berjalan.

Saat GitHub Actions membangun APK:
1. workflow menjalankan `scripts/fetch-jsqr.sh`;
2. script mengambil `dist/jsQR.js` dari commit GitHub yang dipin:
   `8e6a036beafa7053dd44b1b76ac578d22b1b3311`;
3. Git blob SHA harus sama dengan:
   `99ea9df26907009e5553233ffe03c529c1521739`;
4. file lalu dimasukkan ke `assets/jsQR.js` di dalam APK;
5. sesudah APK terpasang, scanner memakai file lokal tersebut.

Jadi koneksi internet hanya diperlukan pada proses build GitHub, bukan saat aplikasi dipakai.

## Artifact yang diharapkan

Debug/testing:

`Quick-Identify-Equipment-V5.4.1-offline-debug`

Release bila signing secrets tersedia:

`Quick-Identify-Equipment-V5.4.1-offline-release`

## Signing untuk update berikutnya

Untuk APK yang akan dipakai jangka panjang, gunakan satu keystore tetap melalui GitHub Actions secrets:

- `QIE_KEYSTORE_BASE64`
- `QIE_KEYSTORE_PASSWORD`
- `QIE_KEY_ALIAS`
- `QIE_KEY_PASSWORD`

Jangan mengganti keystore setelah aplikasi mulai dibagikan. Package ID final tetap:

`com.wahyusetiyonyoto.quickidentifyequipment`

## Pengujian minimum setelah build

1. Instal APK tanpa koneksi internet.
2. Aktifkan airplane mode.
3. Buka dashboard.
4. Cari beberapa equipment.
5. Buka lubricant details.
6. Scan QR menggunakan kamera.
7. Scan QR dari galeri.
8. Tampilkan QR equipment existing.
9. Tambahkan equipment test dan pastikan QR dapat dibuat tanpa internet.
10. Export CSV/PNG.
11. Tutup aplikasi dan buka kembali.
12. Pastikan data localStorage tetap tersedia.

Jika seluruh poin tersebut lolos, build dapat dianggap memenuhi target runtime offline.

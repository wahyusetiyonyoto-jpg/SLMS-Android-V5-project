# Offline Runtime Test Checklist — Quick Identify Equipment 5.4.1

Lakukan pengujian pada APK hasil GitHub Actions, bukan pada HTML biasa.

## Installation
- [ ] APK dapat diinstal pada Android target.
- [ ] App name tampil `Quick Identify Equipment`.
- [ ] Logo launcher memakai logo gear + oil-drop dari HTML.
- [ ] Package ID: `com.wahyusetiyonyoto.quickidentifyequipment`.
- [ ] Version: `5.4.1` / versionCode 10.

## Airplane Mode
Aktifkan Airplane Mode sebelum membuka aplikasi.
- [ ] Dashboard terbuka normal.
- [ ] Equipment list terbuka.
- [ ] Lubrication database terbuka.
- [ ] Search/filter equipment berjalan.
- [ ] Detail lubricant/capacity tampil.
- [ ] Tidak ada spinner/error karena resource internet.

## QR
- [ ] QR untuk equipment existing tampil tanpa internet.
- [ ] Scan QR dengan kamera berhasil.
- [ ] Scan QR dari galeri berhasil.
- [ ] Equipment hasil scan ditemukan dengan benar.
- [ ] Equipment baru/test dapat menghasilkan QR tanpa internet.

## Data
- [ ] Tambah/edit data lalu tutup aplikasi.
- [ ] Buka kembali dan pastikan perubahan tetap ada.
- [ ] Export CSV berhasil.
- [ ] Export PNG/QR berhasil.
- [ ] Print label dapat membuka Android Print dialog.

## Database 2026 spot checks
- [ ] `02-50KN001` gearbox: 2 Litres.
- [ ] `02-50KN002` gearbox: 2 Litres.
- [ ] `03-10CP005` compressor: TURBO T 68 / 180 Litres.
- [ ] `03-10CP006` compressor: TURBO T 68 / 180 Litres.
- [ ] `02-80XM601` hydraulic system: 800 Litres.
- [ ] `02-80XM603` hydraulic system: 800 Litres.
- [ ] Source rows dengan capacity kosong tetap kosong.

## Release discipline
- [ ] APK produksi dibangun dengan keystore tetap dari GitHub Actions Secrets.
- [ ] Keystore tidak diganti antar versi.
- [ ] Production keystore/password tidak pernah di-commit ke repository.

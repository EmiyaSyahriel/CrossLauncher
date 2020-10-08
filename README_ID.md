Bahasa : Bahasa Indonesia | [English](https://github.com/EmiyaSyahriel/CrossLauncher/blob/master/README.md)

# Cross Launcher
Peluncur Aplikasi Android mirip XMB pada beberapa produk Sony, terutama XMB PlayStation 3.

## Fokus Utama
Launcher ini tidak benar-benar ditujukan kepada perangkat Android layar sentuh, Tetapi untuk perangkat
Android yang normalnya tidak memiliki antarmuka sentuh seperti TV, Komputer, Laptop atau Emulator.

Peluncur ini masih bisa dipakai pada perangkat Android layar sentuh, hanya saja akan memerlukan usaha
lebih untuk navigasinya.
**Anda pasti sudah tahu bahwa ada perangkat Android yang bukan HP. Jangan sebarkan kebodohan dengan nge-toxic!**

## Penggunaan
| Fungsi            | Keyboard   | DualShock | Xbox     | Layar Sentuh     |
|-------------------|------------|-----------|----------|------------------|
| Memilah Item      |Tombol Arah | Arah      | Arah     | Geser            |
| Membuka Item      | Enter      | X/O       | A/B      | Sentuh pada ikon |
| Opsi*             | Menu/Tab   | Segitiga  | Y        | Sentuh dua jari  |

*Belum sempurna

## Penggunaan di TV
**Perhatian: Peluncur ini masih belum siap untk dipakai secara default di TV, resiko ditanggung sendiri.**

Beberapa TV tidak mengizinkan penggunaan Peluncur external. Launcher ini bisa dipakai secara default dengan cara berikut.
Asumsi anda menggunakan Windows. Untuk langkah 4 - 8 harus urut, terutama 6 dan 7.

1. Aktifkan Opsi Pengembang dan USB Debugging di TV.
2. Pasang Driver TV anda dan ADB dengan Fastboot
3. Download APK peluncur ini.
4. Sambungkan TV anda ke PC.
5. Di PC, jalankan "cmd".
6. Pasang peluncur ini dengan:
```
adb install "path/to/apk"
```
7. Uninstall peluncur bawaan dengan:
```
adb uninstall -k --user 0 com.google.android.leanbacklauncher
adb uninstall -k --user 0 com.google.android.tvlauncher
```
8. Sekarang, launcher ini sudah menjadi launcher default TV anda.


## Penggunaan RAM
Pada perangkat tes saya, aplikas kosongan biasa memakan RAM sekitar 10MB, ditambah ikon yang dimuat sekitaran 
500kB~1MB per ikon. Ikon akan tetap berada pada RAM selama launcher berjalan.

## Fitur
- [x] Ganti tombol konfirmasi (Pakai Bulat atau Silang)
  - [ ] Belum tersedia di menu
- [x] Deteksi aplikasi game dengan algoritma bawaan Android dan penyocokan kamus
- [x] Galeri musik dan video
- [x] Pemutar musik minimal
- [x] Dukungan gamepad (belum total)
- [x] Cari item dengan tombol alphabet (Keyboard)
- [x] Animasi saat Mulai dan saat Luncur (Bisa di atur), lihat 
[Memodifikasi Animasi](https://github.com/EmiyaSyahriel/CrossLauncher/blob/master/README_ID.md#memodifikasi-animasi) untuk cara modifikasi
- [ ] Layar Dialog seperti XMB
  - [x] Masih tahap awal
- [ ] Opsi Item (Segitiga di PS3)
- [ ] Menyembunyikan Item
- [ ] Buka video dengan aplikasi default
- [ ] Bisa memuat ikon dan BGM kustom
- [ ] Media player bawaan aplikasi
  - [x] Musik (Awal)
  - [ ] Video (Ada, belum terpakai)

## TODOs:
- ~Menghitung padding sehingga tidak ada item penting yang tertutup bar sistem~
- Men soft-coding kan teks
- Optimisasi pada pemuatan dan pen-cache-an item
- Membuat media player di Activity terpisah namun terintegrasi ke Activity launcher utama
- Memakai sistem konfigurasi berbasis biner, daripada menggunakan SharedPref bawaan Android
- Memambahkan petunjuk mapping untuk lebih banyak layout gamepad (XBox, Nintendo Switch)
- Membuat ViewGroup berbasis Dialog XMB yang bisa dipakai di layout XML
- Mengimplementasikan pemuatan ikon, gambar latar dan BGM mirip dengan struktur PS3 Content Info File (ICON0.png, SND0.aac, PIC0.png)
- Membuat sistem vertical sliding yang lebih baik untuk layar sentuh

## Memodifikasi Animasi
Anda bisa memodifikasi animasi saat mulai dan saat luncur dengan menambah
atau mengganti file di `/sdcard/Android/data/id.psw.vshlauncher/files/`.
| Nama file    | Peran           |
|--------------|-----------------|
| coldboot.png | Logo mulai      |
| coldboot.mp3 | Suara mulai     |
| gameboot.png | Logo luncur     |
| gameboot.mp3 | Suara luncur    |

## Download
[👉 Dimari gan 👈](https://github.com/EmiyaSyahriel/CrossLauncher/releases), atau silahkan coba build sendiri.

## Kontribusi
Bisa dalam bentuk terjemahan dan perbaikan.

Saya masih satu setengah tahun ini membuat aplikasi Android, dan diluar kapasitas profesional.

## Lisensi
MIT License.

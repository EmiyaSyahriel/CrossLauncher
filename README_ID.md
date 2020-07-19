Bahasa : Bahasa Indonesia | [English](https://github.com/EmiyaSyahriel/CrossLauncher/blob/master/README.md)

# Cross Launcher
Peluncur Aplikasi Android mirip XMB pada beberapa produk Sony, terutama XMB PlayStation 3.

## Fokus Utama
Launcher ini tidak benar-benar ditujukan kepada perangkat Android sentuh, Tetapi untuk perangkat
Android yang normalnya tidak memiliki antarmuka sentuh seperti TV, Komputer, Laptop atau Emulator.

Peluncur ini masih bisa dipakai pada perangkat Android sentuh, hanya saja akan memerlukan usaha
lebih untuk navigasinya.

## Penggunaan
| Fungsi            | Keyboard   | DualShock | Xbox     | Sentuh           |
|-------------------|------------|-----------|----------|------------------|
| Memilah Item      |Tombol Arah | D-Pad     | D-Pad    | Geser            |
| Execute Item      | Enter      | X/O       | A/B      | Sentuh pada ikon |
| Hide/Show Options*| Menu/Tab   | Segitiga  | Y        | Sentuh dua jari  |

*Belum sempurna

## Penggunaan RAM
Pada perangkat saya, kosongannya biasa memakan 10MB, ditambah ikon yang dimuat sebesar 500kB~1MB per ikon.
Ikon akan tetap berada pada RAM selama launcher berjalan.

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
Beberapa baris kode boleh, Asal tidak merubah fungsionalitas utama XMB. Atau mungkin beberapa nasihat
dalam pemrogramman.

Saya masih satu setengah tahun ini membuat aplikasi Android, dan diluar kapasitas profesional.

## Lisensi
MIT License.

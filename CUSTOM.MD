# Supported Customizations
You can customize many resources by adding a file matching the paths listed in this file, 
supports Internal storage by default and optionally external storage if granted, with 
these supported prefixes (further written as `${PREFIX}`, sorted by load priority):
- `/storage/emulated/0/Android/data/id.psw.vshlauncher/files`
- `/sdcard/Android/data/id.psw.vshlauncher/files` (on devices with lower Android devices)
- `/storage/XXXX-XXXX/Android/data/id.psw.vshlauncher/files` *(If external storage access is granted)*

### Launcher
#### Launcher font
- at `${PREFIX}/dev_flash/data/font/VSH-CustomFont.ttf`

If this file is unavailable, system font will be used
#### Cold Boot (Launcher initial boot)
- Audio at `${PREFIX}/dev_flash/vsh/resource/COLDBOOT.MP3`
- Image at `${PREFIX}/dev_flash/vsh/resource/COLDBOOT.PNG` (WIP)

#### Game Boot (When launching app)
- Audio at `${PREFIX}/dev_flash/vsh/resource/GAMEBOOT.MP3`
- Image at `${PREFIX}/dev_flash/vsh/resource/GAMEBOOT.PNG` (WIP)

#### UI Sound Effect
- at `${PREFIX}/dev_flash/vsh/resource/sfx/`
- Supported extensions : OGG, MP3, WAV
- File names : 
  - `cancel.???` : Cancelation (Subcontent backstep, Menu closing)
  - `confirm.???` : Confirmation (Subcontent open, Launching item)
  - `select.???` : Selection Change (Navigating up/down/left/right)

### Per-Item
Item customization file layout conforms the [PS3 Content Information Files](https://www.psdevwiki.com/ps3/Content_Information_Files#Games_PS3),
Stored at `${PREFIX}\dev_hdd0\games\{App Package ID}_{Activity ID}\`, this directory can be created automatically from Item Menu in the launcher 
#### Icon : `ICON0.???`
- Supported extensions : PNG, JPG

#### Video / Animated Icon : `ICON1.???`
- Supported extensions : WEBP, APNG, MP4, GIF
Can be disabled from system menu

#### Backdrop Image : `PIC1.???`
- Supported extensions : PNG, JPG
This image will become the base background if overlay backdrop is available 

#### Overlay Backdrop Image (WIP) : `PIC0.???`
- Supported extensions : PNG, JPG

#### Background Sound : `SND0.???`
- Supported extensions : MP3, AAC, MID / MIDI

#### Portrait Backdrop Image (WIP) : `PIC1_P.???`
- Supported extensions : PNG, JPG

#### Overlay Portrait Backdrop Image (WIP) : `PIC0_P.???`
- Supported extensions : PNG, JPG
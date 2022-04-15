# Supported Customizations
You can customize many resources by putting a suitable file matching the paths listed here, 
supports Internal storage by default and optionally external storage if granted, with 
these supported prefixes (further written as `${PREFIX}`, sorted by load priority):
- `/storage/emulated/0/Android/data/id.psw.vshlauncher/files`
- `/sdcard/Android/data/id.psw.vshlauncher/files` **(on lower Android version)**
- `/storage/XXXX-XXXX/Android/data/id.psw.vshlauncher/files` **(If external storage access is granted)**

### Launcher
#### Launcher font
- at `${PREFIX}/dev_flash/data/font/VSH-CustomFont.ttf`

If this file is unavailable, system font will be used. Some device would use a glyph from 
system font if current font didn't have the glyph (Fallback-ing). However, there is also 
devices which wouldn't do it and will just render a square glyph instead, make sure that you supply
a complete Unicode font in case your phone is one of it. There is some tools to compile several 
fonts into a single Unicode font.
  
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
- Best Size: 320x176, 20:11 Aspect Ratio
You may supply lower resolution image to save RAM and Storage,
It will be fitted and scaled accordingly. This also applies to Animated Icon

#### Video / Animated Icon : `ICON1.???`
- Supported extensions : WEBP, APNG, MP4, GIF
- Best Size: 320x176, 20:11 Aspect Ratio
Can be disabled from `Settings > System Settings > Disable Video Icon`

#### Backdrop Image : `PIC1.???`
- Supported extensions : PNG, JPG
- Best Size: Device Resolution
Image will be rendered with fill scaling based on viewport resolution, part of the image will be
rendered off-screen to make sure that the entire viewport is filled.
This image will become the base background if overlay backdrop is available 

#### Overlay Backdrop Image : `PIC0.???`
- Supported extensions : PNG, JPG
- Best Size: Device Resolution
Image will be rendered with fit scaling based on viewport resolution to make sure that every part
 of the image is on-screen

#### Background Sound : `SND0.???`
- Supported extensions : MP3, AAC, MID / MIDI

#### Portrait Backdrop Image : `PIC1_P.???`
- Supported extensions : PNG, JPG
- Best Size: Device Resolution
Will be prioritized and displayed only if viewport orientation is portrait

#### Overlay Portrait Backdrop Image : `PIC0_P.???`
- Supported extensions : PNG, JPG
- Best Size: Device Resolution
Will be prioritized and displayed only if viewport orientation is portrait
  
#### Item Settings : `PARAM.INI`
Saved as a normal INI-like file format with the INI section being the language code to be used, contains values that will change launcher behaviours 
on this item, that is:
- `DisplayName` - String - Name of the app when displayed in the launcher
- `Album` - String - App Album
- `ParentalRestriction` - Boolean - Is this app restricted to the children, requires password (WIP) to run
- `Hidden` - Boolean - Is the app hidden away from the launcher
# Euphoriae

<p align="center">
  <img src="logo/logo.png" width="120" alt="Euphoriae Logo"/>
</p>

<p align="center">
  <b>A modern local music player for Android</b><br>
  <i>Euphoriae is a free & open source music player inspired by the Greek word “Euphoria.”</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/API-25%2B-blue?style=flat-square" alt="API"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple?style=flat-square" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-teal?style=flat-square" alt="Compose"/>
</p>

---

## Features

- **Local Music Playback**: Play your local audio files with high-quality ExoPlayer
- **Playlist Management**: Create, edit, and manage your playlists
- **Equalizer**: EQ with presets, bass boost & virtualizer
- **Material You**: Dynamic theming based on your wallpaper
- **Playback Modes**: Shuffle and repeat (all/one) support
- **Search**: Quickly find your songs
- **Modern UI**: Clean, intuitive interface with smooth animations
- **Typography**: Plus Jakarta Sans

## Screenshots

<p align="center">
  <img src="screenshoots/Screenshot_20251224-173516_Euphoriae.png" width="200" alt="Home Screen"/>
  <img src="screenshoots/Screenshot_20251224-173521_Euphoriae.png" width="200" alt="Songs List"/>
  <img src="screenshoots/Screenshot_20251224-173528_Euphoriae.png" width="200" alt="Playlist"/>
  <img src="screenshoots/Screenshot_20251224-173539_Euphoriae.png" width="200" alt="Playlist Detail"/>
  <img src="screenshoots/Screenshot_20251224-173544_Euphoriae.png" width="200" alt="Playlist Songs"/>
</p>

<p align="center">
  <img src="screenshoots/Screenshot_20251224-173555_Euphoriae.png" width="200" alt="Now Playing"/>
  <img src="screenshoots/Screenshot_20251224-173600_Euphoriae.png" width="200" alt="Now Playing 2"/>
  <img src="screenshoots/Screenshot_20251224-173608_Euphoriae.png" width="200" alt="Search"/>
  <img src="screenshoots/Screenshot_20251224-173621_Euphoriae.png" width="200" alt="Equalizer"/>
  <img src="screenshoots/Screenshot_20251224-173623_Euphoriae.png" width="200" alt="Equalizer 2"/>
</p>

<p align="center">
  <img src="screenshoots/Screenshot_20251224-173628_Euphoriae.png" width="200" alt="Settings"/>
  <img src="screenshoots/Screenshot_20251224-173632_Euphoriae.png" width="200" alt="Settings 2"/>
  <img src="screenshoots/Screenshot_20251224-173635_Euphoriae.png" width="200" alt="Settings 3"/>
  <img src="screenshoots/Screenshot_20251224-173645_Euphoriae.png" width="200" alt="Light Mode"/>
</p>

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 |
| Media | Media3 ExoPlayer & MediaSession |
| Database | Room Persistence Library |
| Image Loading | Coil |
| Architecture | MVVM with StateFlow |

## Getting Started

### Prerequisites
- Android Studio Otter 2
- Android SDK 25+
- Kotlin 2.0+

### Build
```bash
# Clone the repository
git clone https://github.com/ellenoireQ/euphoriae.git

# Open in Android Studio and sync Gradle

# Run on device/emulator
```

## Project Structure

```
app/src/main/java/com/oss/euphoriae/
├── data/
│   ├── class/          # Player & Audio Effects
│   ├── local/          # Room Database & DAO
│   ├── model/          # Data classes (Song, Playlist)
│   └── repository/     # Music Repository
├── service/            # Media Playback Service
├── ui/
│   ├── components/     # Reusable UI components
│   ├── screens/        # App screens
│   ├── theme/          # Material You theming
│   └── viewmodel/      # ViewModels
├── EuphoriaeApp.kt     # Application class
└── MainActivity.kt     # Main entry point
```

## License

```
  Copyright 2025 Euphoriae
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```

## 🤝 Contributing

Contributions are welcome! Feel free to:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


---



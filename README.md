# REPLAY 99

> **"Your music. Your machine. Your rules."**

REPLAY 99 is a complete native Android music application built with Kotlin, Jetpack Compose, AndroidX Media3 / ExoPlayer, Room, and MediaSessionService. It is designed to feel like owning an authentic collection of underground 1990s music equipment.

---

## 1. Visual Language & Aesthetics

- **Color Palette:** Matte black (`#0C0E10`), Gunmetal (`#14171A`, `#1D2126`), Brushed Aluminium (`#5A626B`), with selective underground accents in Acid Green (`#00FF66`), Burnt Orange (`#FF6B1A`), and Electric Cyan (`#00E5FF`, `#38E1FF`).
- **Typography:** Bundled industrial and electronic fonts (`Share Tech Mono` for LCD displays/counters and `Chakra Petch` for hardware bevels and control markings) installed locally via `font-util`.
- **Hardware Controls:** Beveled physical buttons, rotary knobs, grooved fader sliders, segment VU meters, toothed cassette spools, and spinning compact discs.

---

## 2. Architecture & Destinations

The application provides a unified hardware navigation rack routing between 8 primary destinations and 6 expansion bays:

1. **Home / Control Deck:** Central machine console with cassette reel transport, dual stereo VU meters, expansion bay quick-jump buttons, most-played crate rotation, and master library track list.
2. **Cassette Player:** Interactive vintage cassette deck with tape reel physics (spool thickness dynamically changes as tape winds from spool A to B), real drag-to-seek, Side A / Side B tape flipping, and cassette shell/reel color and sticker customizer.
3. **Discman Player:** Portable 1990s CD player with spinning laser disc, electronic shock protection (ESP 45s buffer display), physical repeat/shuffle toggles, and digital booklet jewel case inspection modal.
4. **DJ Deck:** Dual turntables (Deck A and Deck B) with independent track loaders, live scratchable vinyl platters (touch drag alters playback position and playback speed for realistic scratching), optical crossfader, pitch sliders (-16% to +16%), and 3-band isolator EQ.
5. **Lyrics Room:** Synchronized LRC karaoke engine with line highlighting, word-by-word active timing, tempo offset tuner (+/- 100ms), and built-in LRC lyric timing editor.
6. **Mixtape Library:** Crate vault with search by tracks, albums, artists, folders, and custom mixtapes. Includes a full **Mixtape Creator** (C-60 / C-90 tape duration, Side A & B track assignments, shell coloring, and direct "Load Deck" playback).
7. **Sound Laboratory:** 5-band graphic equalizer (-12dB to +12dB), Mega Bass rotary boost, 3D Surround virtualizer, hardware presets ("90s Rave Club", "Tape Cassette Warmth", "Discman Crystal", "Underground Bass"), and 4 real-time visualizer modes (Spectrum, Oscilloscope, Circular, Dual Analog Needles).
8. **Workshop:** Machine fabrication customizer allowing selection of physical chassis materials (Matte Black, Gunmetal Steel, Brushed Aluminum, 1994 Worn Plastic), accent LED colors, and LCD matrix backlights.
9. **Collection Room:** Virtual 1990s underground bedroom/studio with physical cassette racks, CD jewel towers, and 12" white-label vinyl crates—tap any tape/CD to load directly into the deck!
10. **Recording Booth:** Tape deck microphone recorder with live VU input level meter, spoken mixtape intro voiceover recording, and persistent recording library.
11. **Radio Mode:** Analog FM tuner (88.0 - 108.0 MHz) with sliding needle dial, tuning noise simulation, stereo lock indicator, saved presets, and local DJ library radio shuffle.
12. **Discovery Machine:** 90s tape roulette randomizer, forgotten cuts finder, and genre crate filter (Acid Techno, Drum & Bass, 90s Hip Hop, Eurodance, Lo-Fi Dub).
13. **Obsession Mode:** Consecrated shrine for single-track repeat obsessions with 99-replay target tracker and personal liner notes.
14. **Music Diary:** Telemetry and listening statistics tracking total plays, heavy rotation graphs, and timestamped session logs.

---

## 3. Playback Engine & Offline Seeder

- **Background Playback:** Implemented via `ReplayPlaybackService` extending AndroidX Media3 `MediaSessionService` with foreground service notifications, audio focus handling, and noisy intent disconnect triggers.
- **Audio Seeder:** REPLAY 99 includes an offline 90s audio synthesizer (`AudioSeeder.kt`) that generates 5 authentic underground 44.1kHz 16-bit WAV tracks with rich multi-harmonic synthesis (Acid 303, Jungle Amen, Boom Bap, Cyber Eurodance, Lo-Fi Dub) so the user has immediate playable music and synchronised lyrics right out of the box, in addition to automatic scanning of all music on device storage via Android `MediaStore`.

---

## 4. Feature Implementation Register

| Feature Name | Status | Source Files | Dependencies / APIs |
|---|---|---|---|
| Control Deck Central Console | Implemented | `ControlDeckScreen.kt`, `HardwareComponents.kt` | Jetpack Compose, Media3 |
| Interactive Cassette Deck | Implemented | `CassettePlayerScreen.kt`, `InteractiveCassetteDeck` | Compose Canvas, Touch Gestures |
| Portable Discman Player | Implemented | `DiscmanPlayerScreen.kt`, `DiscmanGraphic` | Compose Canvas, Media3 |
| Dual DJ Deck & Vinyl Scratch | Implemented | `DjDeckScreen.kt`, `VinylPlatter.kt` | Touch Angle Tracker, ExoPlayer |
| Synchronized Lyrics & Karaoke | Implemented | `LyricsRoomScreen.kt`, `LrcParser.kt` | Custom LRC Regex Parser |
| Mixtape Vault & Tape Creator | Implemented | `MixtapeLibraryScreen.kt`, `ReplayRepository.kt` | Room Database, MediaStore |
| Sound Laboratory & Visualizers | Implemented | `SoundLaboratoryScreen.kt`, `AudioEffectsManager.kt` | Android AudioFX, Canvas |
| Hardware Workshop & Skins | Implemented | `WorkshopScreen.kt`, `Models.kt` | Jetpack Compose |
| Virtual Collection Room | Implemented | `VirtualRoomScreen.kt` | Compose LazyRow / Canvas |
| Recording Booth & Mic Input | Implemented | `RecordingBoothScreen.kt`, `ReplayAudioEngine.kt` | Android MediaRecorder |
| Analog FM Tuner Radio | Implemented | `RadioModeScreen.kt` | Compose Canvas, Local Shuffle |
| Music Discovery & Roulette | Implemented | `DiscoveryMachineScreen.kt` | Kotlin Randomizer |
| Obsession Mode Shrine | Implemented | `ObsessionModeScreen.kt`, `Entities.kt` | Room Database |
| Music Diary & Statistics | Implemented | `MusicDiaryScreen.kt`, `Entities.kt` | Room Database |
| Background Playback Service | Implemented | `ReplayPlaybackService.kt`, `AndroidManifest.xml` | MediaSessionService, ExoPlayer |
| Custom 90s Adaptive Icon | Implemented | `ic_launcher.xml`, `ic_launcher_round.png` | Android Adaptive Icon, ImageMagick |

---

## 5. Build & Verification

```bash
# Verify compilation:
gradle assembleDebug

# Run unit and Robolectric tests:
gradle :app:testDebugUnitTest
```

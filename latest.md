- feat: Implement Tempo/Pitch control, Hi-Res audio, and Stability fixes:
    ## Audio Engine Overhaul

    ### 10-Band Graphic Equalizer
    - Full 10-band EQ: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
    - 12 built-in presets: Flat, Bass Boost, Rock, Pop, Jazz, Classical, Hip Hop, Electronic, Vocal, R&B

    ### Effect Profiles
    - Custom, Music, Movie, Game, Podcast, Hi-Fi presets
    - One-tap profile switching

    ### Surround Sound / 3D Audio
    - Surround modes: Off, Music, Movie, Game, Podcast
    - 3D Effect, Room Size, Surround Level controls

    ### Headphone Optimization
    - Headphone type selection: Generic, In-ear, On-ear, Over-ear, Earbuds
    - Headphone surround toggle

    ### Dynamic Processing
    - Compressor - Reduce dynamic range
    - Volume Leveler - Normalize loudness
    - Limiter - Prevent clipping
    - Dynamic Range control

    ### Audio Enhancement
    - Clarity - High frequency detail
    - Spectrum Extension - Restore high frequencies
    - Tube Amp Warmth - Analog simulation
    - Treble Boost

    ### Reverb Engine
    - 7 presets: None, Small Room, Medium Room, Large Room, Medium Hall, Large Hall, Plate

    ### Playback Control
    - Tempo & Pitch
    - Crossfade

    ### Stereo & Balance
    - Stereo Balance (Left/Right pan)
    - Channel Separation (Stereo width)


    ### UI Improvements
    - Collapsible sections for all effect categories
    - Premium gradient cards
    - Audio Output Info panel

    ### Native DSP Engine
    All effects processed in real-time C++ for low latency:
    - **Compressor** - RMS envelope follower with configurable ratio
    - **Limiter** - Soft tanh clipping for headroom protection
    - **3D Surround** - Haas effect with adjustable delay
    - **Clarity** - High-shelf frequency boost
    - **Tube Amp** - Asymmetric waveshaping for warmth
    - **Spectrum Extension** - Harmonic generation
    - **Volume Leveler** - RMS-based normalization
    - **Treble Boost** - High-pass filter enhancement
    - **Tempo & Pitch** - Tempo & Pitch
    - **Crossfade** - Crossfade between tracks
    - **Stereo Balance** - Equal-power panning
    - **Channel Separation** - Stereo width control

### Other Improvements
- equalizerScreen: add `Beta` flag
- equalizerScreen: change `AudioEngine` to `Equalizer`
- feat: Fix Now Playing not showing when opening from widget
- feat: implement global equalizer settings persistence
- fix: Enable widget next/prev with queue-based navigation
- feat: Add music player home screen widget with Glance API
- feat: Implement feature Timed Lyrics
- feat: enhance playlist ui and dynamic covers
- feat: enhance album grouping and card ui
- feat: add playlist creation from albums

# Compass

A minimal and modern native Android compass built with Kotlin and Jetpack Compose, featuring a clean Material You / Pixel-inspired interface.

## Features

- 🧭 Real-time sensor-based compass
- 🧲 Magnetic North and True North
- 📍 Automatic magnetic declination using device location
- 🎨 Material You Dynamic Color
- 🌗 Light, Dark and System themes
- 🌑 AMOLED-inspired Dark Theme
- 🔷 Four dial styles:
  - Sunny
  - Circle
  - Diamond
  - Octagon
- 🔄 Smooth real-time dial rotation
- 🔒 Dial position lock
- 📳 Optional haptic feedback
- 🎯 Compass calibration
- ⚙️ Persistent settings
- 🧩 Home-screen Compass widgets
- 🎨 Widget-specific dial shape selection
- 📱 Dynamic Color support for widgets
- 🔗 Widget opens the main Compass app

## Widgets

The app includes native Android home-screen widgets with the same Compass dial designs used in the main app.

Each widget can independently use one of the four dial shapes:

**Sunny · Circle · Diamond · Octagon**

Widgets support live compass movement and Material You Dynamic Color, adapting to the device's current wallpaper/theme.

## Design

Compass follows a minimal, spacious and modern Android design language inspired by Google's Pixel and Material You ecosystem.

The UI uses dynamic wallpaper-based colors, adaptive themes, rounded surfaces and smooth motion while keeping the compass experience focused and distraction-free.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Sensor Framework
- Android Location APIs
- Material You Dynamic Color
- Native Android App Widgets

## Compass Modes

### Magnetic North
Displays the direction based on the Earth's magnetic field detected by the device.

### True North
Uses the device's location and magnetic declination to compensate for the difference between magnetic north and geographic north.

## License

This project is available for personal and educational use.

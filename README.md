# Compass

A minimal, native Android compass built with Kotlin and Jetpack Compose, designed with a clean Material You / Pixel-inspired interface.

## Features

- 🧭 Real-time compass heading using device sensors
- 🧲 Magnetic North and True North support
- 📍 Location-based magnetic declination
- 🎨 Material You Dynamic Color support
- 🌙 Light, Dark, and System themes
- 🔷 Multiple compass dial shapes:
  - Sunny
  - Circle
  - Diamond
  - Octagon
- 🔄 Smooth real-time dial rotation
- 🔒 Dial position lock
- 📳 Optional haptic feedback for cardinal directions
- 🛠️ Built-in compass calibration flow
- ⚙️ Dedicated settings with persistent preferences
- ♿ Accessibility-focused controls and touch targets
- 📱 Native Android UI with responsive layouts

## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Android Sensor Framework**
- **Android Location APIs**
- **Material You Dynamic Color**

## Design

The app follows a minimal, spacious and modern Android design language inspired by Google's Pixel and Material You ecosystem.

The interface uses dynamic wallpaper-based colors, adaptive light/dark themes, rounded surfaces and a distraction-free compass experience.

## Compass Modes

### Magnetic North
Uses the Earth's magnetic field as detected by the device's sensors.

### True North
Uses the device's location and magnetic declination to compensate for the difference between magnetic north and geographic north.

## Project Structure

The project is built as a native Android application using Jetpack Compose and follows a modular, maintainable architecture with separate handling for UI, sensors, location, preferences and theme management.

## License

This project is available for personal and educational use.

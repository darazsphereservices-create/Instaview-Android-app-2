# InstaView Android — starter MVP

This is a native Android Studio project using Kotlin and Jetpack Compose.

## Included in version 0.1.0
- Instagram-inspired Android interface.
- Public Instagram profile URL validation and opening in a Custom Tab.
- DownloadManager support for direct HTTPS video files ending in `.mp4`, `.m4v`, or `.webm`.
- Downloads are saved in the device Downloads folder.

## Important limitations
This starter does **not** scrape Instagram profiles or extract Instagram Reel media URLs. Instagram does not provide an unrestricted public API for downloading arbitrary public posts. Do not use this app to bypass private-account access controls, login restrictions, or copyright protections. Only download media you own or have permission to save.

The downloader currently accepts direct video-file URLs, not Instagram profile, post, or Reel page URLs. Some servers may also reject DownloadManager requests or require authorization headers; those cases need a supported integration.

## Build
1. Install Android Studio (current stable) and a JDK 17-compatible setup.
2. Open this folder in Android Studio.
3. Allow Gradle sync and install the Android SDK Platform 35 if prompted.
4. Connect an Android phone with USB debugging enabled, or start an emulator.
5. Press Run.

To create an APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

Package: `com.example.instaview`
Minimum Android: 8.0 (API 26)
Target SDK: 35

## Next development milestone
Add a compliant media-provider integration for authorized/public content, then implement a native profile/post gallery using the provider's permitted metadata and media URLs. Avoid HTML scraping or reverse-engineered private endpoints.

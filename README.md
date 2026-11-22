# InstaView Android

Native Android app using Kotlin and Jetpack Compose. Version 0.2.0 adds a best-effort public Instagram Reels loader and authorized Reel download flow.

## Open-source components
- Jetpack Compose and AndroidX Browser
- [youtubedl-android](https://github.com/yausername/youtubedl-android) 0.18.1, which bundles yt-dlp, Python and QuickJS for Android
- FFmpeg Android module for merging best video/audio streams

The youtubedl-android project is GPL-3.0. Distributing an APK that includes it requires compliance with GPL-3.0, including providing corresponding source and preserving notices. Review all bundled component licenses before publishing.

## Current features
- Enter a public Instagram username or profile URL.
- Ask yt-dlp for up to 20 public profile Reels (best effort).
- Resolve and play a selected Reel when Instagram exposes a playable stream.
- Download a selected Reel using yt-dlp's best available video/audio format into `Downloads/InstaView`.
- No InstaView watermark is added.

## Limitations and responsible use
Instagram can require login, rate-limit requests, or change its site in ways that break extraction. Public profile listing, playback and downloading are therefore not guaranteed. This app does not bypass private-account controls, login walls, DRM, or other access restrictions. Only download videos you own or have explicit permission to save; public visibility alone is not permission. The downloader may fail when Instagram requires an authenticated session or blocks automated requests.

This first integration does not import Instagram browser cookies. Do not add session cookies or credentials to source control. The profile list is a best-effort yt-dlp playlist response, not an official Instagram API.

## Build
1. Open the repository in Android Studio with JDK 17.
2. Sync Gradle (Maven Central is required to resolve youtubedl-android).
3. Build and install the debug APK on an ARM or emulator device.
4. Test with a public profile that permits unauthenticated viewing.

The yt-dlp Android runtime and FFmpeg increase APK size. The first launch initializes the bundled runtime.

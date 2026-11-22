# InstaView Android 0.2.0

Native Kotlin + Jetpack Compose app using the open-source yt-dlp Android wrapper and FFmpeg.

## Implemented
- Validate a public Instagram username/profile URL.
- Best-effort public profile Reels listing via yt-dlp.
- Per-Reel Play action resolves a stream and previews it with Android VideoView.
- Per-Reel Download action uses yt-dlp and prefers the best MP4 format.
- Downloads are saved under the app-specific external Downloads/InstaView folder.
- InstaView does not add a watermark.

## Limitations
Instagram's current upstream yt-dlp user-profile extractor is marked non-working. Instagram may require login, rate-limit, or change its website. Listing, preview and downloading are best-effort and can fail for public profiles. No login/private-account/access-control bypass is implemented. Only download content you own or have permission to save. This app does not import browser cookies.

## Licenses
The youtubedl-android wrapper is GPL-3.0; yt-dlp is Unlicense; FFmpeg licensing depends on the bundled build configuration. Review all component notices and satisfy GPL source-distribution obligations before distributing APKs.

## Build
Open in Android Studio with JDK 17 and Android SDK Platform 35, sync Gradle, and build the debug APK. Embedded Python/native media libraries increase APK size.

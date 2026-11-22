package com.example.instaview

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL

class InstaViewApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: Exception) {
            Log.e("InstaView", "yt-dlp runtime initialization failed", e)
        }
    }
}

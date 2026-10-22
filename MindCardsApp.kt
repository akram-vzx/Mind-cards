package com.mindcards.app

import android.app.Application
import com.mindcards.app.data.AppRepository

class MindCardsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppRepository.init(this)
        ThemeManager.applyDarkMode(this)
    }
}

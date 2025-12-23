package com.oss.euphoriae

import android.app.Application
import com.oss.euphoriae.data.local.MusicDatabase
import com.oss.euphoriae.data.repository.MusicRepository

class EuphoriaeApp : Application() {
    
    val database by lazy { MusicDatabase.getDatabase(this) }
    val musicRepository by lazy { MusicRepository(this, database.musicDao()) }
    
    override fun onCreate() {
        super.onCreate()
    }
}

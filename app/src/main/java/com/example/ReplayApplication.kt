package com.example

import android.app.Application
import com.example.data.ReplayDatabase
import com.example.data.ReplayRepository
import com.example.playback.ReplayAudioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReplayApplication : Application() {

    lateinit var database: ReplayDatabase
        private set

    lateinit var repository: ReplayRepository
        private set

    lateinit var audioEngine: ReplayAudioEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = ReplayDatabase.getDatabase(this)
        repository = ReplayRepository(this, database.replayDao())
        audioEngine = ReplayAudioEngine(this)

        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeLibrary()
        }
    }
}

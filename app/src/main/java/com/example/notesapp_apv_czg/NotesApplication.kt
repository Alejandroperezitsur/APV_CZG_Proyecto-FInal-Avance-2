package com.example.notesapp_apv_czg

import android.app.Application
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.NotesRepository
import com.example.notesapp_apv_czg.data.OfflineNotesRepository

/**
 * Custom Application class for the app. This is the central place for dependency injection.
 */
class NotesApplication : Application() {

    /**
     * The application-wide repository instance.
     */
    lateinit var container: NotesRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        container = OfflineNotesRepository(database.noteDao())
    }
}

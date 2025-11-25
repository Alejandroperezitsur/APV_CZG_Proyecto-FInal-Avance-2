package com.example.notesapp_apv_czg

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.NotesRepository
import com.example.notesapp_apv_czg.data.OfflineNotesRepository

/**
 * Custom Application class for the app. This is the central place for dependency injection.
 */
class NotesApplication : Application(), ImageLoaderFactory {

    /**
     * The application-wide repository instance.
     */
    lateinit var container: NotesRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        container = OfflineNotesRepository(database.noteDao())
    }

    /**
     * Sets up a custom ImageLoader for Coil that can handle video frames.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}

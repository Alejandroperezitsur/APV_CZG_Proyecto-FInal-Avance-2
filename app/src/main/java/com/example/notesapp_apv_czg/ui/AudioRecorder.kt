package com.example.notesapp_apv_czg.ui

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording(): Uri? {
        return try {
            val audioFile = createAudioFile()
            this.audioFile = audioFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                audioFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun stopRecording(): Uri? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            audioFile?.let {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    it
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context.cacheDir
        return File.createTempFile(
            "NOTE_APP_${timeStamp}_",
            ".mp3",
            storageDir
        )
    }
}

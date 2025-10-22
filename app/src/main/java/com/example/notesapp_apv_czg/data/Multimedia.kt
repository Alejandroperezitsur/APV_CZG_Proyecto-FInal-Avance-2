package com.example.notesapp_apv_czg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "multimedia")
data class Multimedia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val uri: String,
    val description: String? = null,
    val type: String
)

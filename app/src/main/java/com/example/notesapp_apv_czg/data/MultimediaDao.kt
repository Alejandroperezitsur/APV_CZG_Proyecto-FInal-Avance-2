package com.example.notesapp_apv_czg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MultimediaDao {
    @Insert
    suspend fun insert(multimedia: Multimedia): Long

    @Query("SELECT * FROM multimedia WHERE noteId = :noteId")
    suspend fun getByNoteId(noteId: Long): List<Multimedia>
}

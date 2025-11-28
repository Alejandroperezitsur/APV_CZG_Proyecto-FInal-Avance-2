package com.example.notesapp_apv_czg.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return if (list == null) null else gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromLongList(list: List<Long>?): String? {
        return if (list == null) null else gson.toJson(list)
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }
}

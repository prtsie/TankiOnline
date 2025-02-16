package com.example.tankionline

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.media.MediaFormat.KEY_LEVEL
import com.example.tankionline.models.Element
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LevelStorage(val context: Context) {
    private val prefs = (context as Activity).getPreferences(MODE_PRIVATE)

    fun saveLevel(elementsOnContainer: List<Element>) {
        prefs.edit()
            .putString(KEY_LEVEL, Gson().toJson(elementsOnContainer))
            .apply()
    }

    fun loadLevel(): List<Element>? {
        val levelFromPrefs = prefs.getString(KEY_LEVEL, null)
        levelFromPrefs?.let {
            val type = object : TypeToken<List<Element>>() {}.type
            return Gson().fromJson(it, type)
        }
        return null
    }
}
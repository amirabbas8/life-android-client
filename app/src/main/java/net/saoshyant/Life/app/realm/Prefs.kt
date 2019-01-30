package net.saoshyant.Life.app.realm


import android.content.Context
import android.content.SharedPreferences

class Prefs private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    var preLoad: Boolean
        get() = sharedPreferences.getBoolean(PRE_LOAD, false)
        set(totalTime) = sharedPreferences
                .edit()
                .putBoolean(PRE_LOAD, totalTime)
                .apply()

    init {

        sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {

        private val PRE_LOAD = "preLoad"
        private val PREFS_NAME = "prefPosts"
        private var instance: Prefs? = null

        fun with(context: Context): Prefs {

            if (instance == null) {
                instance = Prefs(context)
            }
            return instance as Prefs
        }
    }

}

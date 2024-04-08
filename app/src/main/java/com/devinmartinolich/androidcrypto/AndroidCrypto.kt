package com.devinmartinolich.androidcrypto

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

class AndroidCrypto: Application() {

    override fun onCreate() {
        super.onCreate()

        appInstance = this
    }

    companion object {
        private const val USER_PREFERENCES_NAME = "user_preferences"

        var appInstance: AndroidCrypto? = null
            private set

        val Context.prefStore by preferencesDataStore(
            name = USER_PREFERENCES_NAME
        )
    }

}
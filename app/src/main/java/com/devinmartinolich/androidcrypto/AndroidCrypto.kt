package com.devinmartinolich.androidcrypto

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

class AndroidCrypto: Application() {

    private val Context.prefStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )

    val userPrefRepo = UserPreferencesRepository(prefStore, CryptoManager())

    override fun onCreate() {
        super.onCreate()

        appInstance = this
    }

    companion object {
        var appInstance: AndroidCrypto? = null
            private set
    }

}
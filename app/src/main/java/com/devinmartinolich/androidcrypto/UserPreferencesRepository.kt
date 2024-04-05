package com.devinmartinolich.androidcrypto

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

const val USER_PREFERENCES_NAME = "user_preferences"

data class UserPreferences(
    val username: String,
    val userIv: String,
    val password: String,
    val passIv: String
)

object PreferencesKeys {
    val USERNAME = stringPreferencesKey("raspberry")
    val PASSWORD = stringPreferencesKey("blueberry")
    val USERNAME_IV = stringPreferencesKey("nannyberry")
    val PASSWORD_IV = stringPreferencesKey("boysenberry")
}

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>, private val cryptoManager: CryptoManager) {

    suspend fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val result = preferences[key]?: defaultValue
            result
        }

    suspend fun updateUsername(username: String) {
        dataStore.edit {
            val encryption = cryptoManager.encrypt(username.toByteArray())
            it[PreferencesKeys.USERNAME] = encryption.value
            it[PreferencesKeys.USERNAME_IV] = encryption.iv.toString()
        }
    }

    suspend fun updatePassword(password: String) {
        dataStore.edit {
            val encryption = cryptoManager.encrypt(password.toByteArray())
            it[PreferencesKeys.PASSWORD] = encryption.value
            it[PreferencesKeys.PASSWORD_IV] = encryption.iv.toString()
        }
    }

    suspend fun getUsername(): String {
        return decryptValue(PreferencesKeys.USERNAME, PreferencesKeys.USERNAME_IV)
    }

    suspend fun getPassword(): String {
        return decryptValue(PreferencesKeys.PASSWORD, PreferencesKeys.PASSWORD_IV)
    }

    private suspend fun decryptValue(valueKey: Preferences.Key<String>, ivKey: Preferences.Key<String>): String {
        var decrypted = ""
        getPreference(valueKey, "").collect { value ->
            getPreference(ivKey, "").collect { iv ->
                if (value.isNotBlank() && iv.isNotBlank()) {
                    decrypted =
                        cryptoManager.decrypt(value.toByteArray(), iv.toByteArray()).toString()
                }
            }
        }
        return decrypted
    }
}
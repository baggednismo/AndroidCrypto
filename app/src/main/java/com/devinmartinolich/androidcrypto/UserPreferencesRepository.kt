package com.devinmartinolich.androidcrypto

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

const val USER_SECRET = "user_secret"
const val PASS_SECRET = "pass_secret"

object PreferencesKeys {
    /**
     * Not ideal to store a username or password with a plain text key
     * that indicates what that value actually is. Keep 'em guessin
     */
    val USERNAME = stringPreferencesKey("raspberry")
    val PASSWORD = stringPreferencesKey("blueberry")
}

/**
 * Class that handles saving and retrieving user preferences
 *
 * @author Devin Martinolich
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>, private val cryptoManager: CryptoManager) {

    /**
     * getPreference() returns a Flow<String> which can be observed within a ViewModel or Fragment/Activity
     * This will emit a value ONLY when the value changes.
     *
     * @param key Preferences.Key<String>
     * @return Flow<String>
     * @author Devin Martinolich
     */
    suspend fun getPreference(key: Preferences.Key<String>): Flow<String> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val result = decryptValue(key, preferences[key] ?: "")
            result
        }

    /**
     * getFirstPreference() returns the LAST value of the first occurrence of a particular key.
     * This is a hard way of collecting a value outside of observing the Flow
     *
     * @param key Preferences.Key<T>
     * @param defaultValue T
     * @return The last value of the first occurrence of a particular key
     * @author Devin Martinolich
     */
    private suspend fun <T> getFirstPreference(key: Preferences.Key<T>, defaultValue: T) :
            T = dataStore.data.first()[key] ?: defaultValue

    /**
     * getPrefDecrypted() is similar to getFirstPreference() however it will additionally attempt
     * to decrypt the result instead of returning it without doing so.
     *
     * @param key
     * @return The decrypted last value of the first occurrence of a particular key
     * @author Devin Martinolich
     */
    suspend fun getPrefDecrypted(key: Preferences.Key<String>) : String = decryptValue(key, getFirstPreference(key, ""))

    /**
     * updateUsername() will encrypt and replace the stored username preference with a new in the
     * format of username.iv
     * This is in that format to keep the iv along with the username for decryption purposes.
     *
     * @param username String
     * @author Devin Martinolich
     */
    suspend fun updateUsername(username: String) {
        dataStore.edit {
            val encryption = cryptoManager.encrypt(username, USER_SECRET)
            it[PreferencesKeys.USERNAME] = encryption
        }
    }

    /**
     * updatePassword() will encrypt and replace the stored password preference with a new in the
     * format of password.iv
     * This is in that format to keep the iv along with the password for decryption purposes.
     *
     * @param password String
     * @author Devin Martinolich
     */
    suspend fun updatePassword(password: String) {
        dataStore.edit {
            val encryption = cryptoManager.encrypt(password, PASS_SECRET)
            it[PreferencesKeys.PASSWORD] = encryption
        }
    }

    /**
     * decryptValue() will attempt to decrypt a provided value.
     *
     * @param key preference that is going to be decrypted Preferences.Key<String>
     * @param value encrypted value String
     * @return decrypted value String
     * @author Devin Martinolich
     */
    private fun decryptValue(key: Preferences.Key<String>, value: String): String {
        println("-> decryptValue(key=$key, value=$value)")
        return if (value.isNotBlank()) {
            cryptoManager.decrypt(value, if (key == PreferencesKeys.USERNAME) USER_SECRET else PASS_SECRET)
        } else ""
    }
}
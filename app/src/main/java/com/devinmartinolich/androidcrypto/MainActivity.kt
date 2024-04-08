package com.devinmartinolich.androidcrypto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devinmartinolich.androidcrypto.AndroidCrypto.Companion.prefStore
import com.devinmartinolich.androidcrypto.ui.theme.AndroidCryptoTheme
import kotlinx.coroutines.launch

const val keystore = "AndroidKeyStore"

class MainActivity : ComponentActivity() {

    private val pref by lazy { AndroidCrypto.appInstance?.prefStore?.let {
        UserPreferencesRepository(it, CryptoManager(keystore)) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidCryptoTheme {
                var username by remember {
                    mutableStateOf("")
                }
                var password by remember {
                    mutableStateOf("")
                }
                var decryptedUser by remember {
                    mutableStateOf("")
                }
                var decryptedPass by remember {
                    mutableStateOf("")
                }
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Username") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Password") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            scope.launch {
                                pref?.updateUsername(username = username)
                                pref?.updatePassword(password = password)
                            }
                        }) {
                            Text(text = "Save")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            scope.launch {
                                pref?.getPrefDecrypted(PreferencesKeys.USERNAME)?.let {
                                    decryptedUser = it
                                }

                                pref?.getPrefDecrypted(PreferencesKeys.PASSWORD)?.let {
                                    decryptedPass = it
                                }
                            }
                        }) {
                            Text(text = "Load")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Decrypted User: $decryptedUser")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Decrypted Pass: $decryptedPass")
                }
            }
        }
    }
}

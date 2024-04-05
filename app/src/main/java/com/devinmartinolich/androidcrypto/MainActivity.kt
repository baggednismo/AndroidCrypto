package com.devinmartinolich.androidcrypto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devinmartinolich.androidcrypto.ui.theme.AndroidCryptoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pref by lazy { AndroidCrypto.appInstance?.userPrefRepo }

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
                var decrypted by remember {
                    mutableStateOf("")
                }
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Username") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Password") }
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
                                decrypted = "${pref?.getUsername()} : ${pref?.getPassword()}"
                            }
                        }) {
                            Text(text = "Load")
                        }
                    }
                    Text(text = decrypted)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

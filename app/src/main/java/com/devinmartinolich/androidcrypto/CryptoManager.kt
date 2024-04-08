package com.devinmartinolich.androidcrypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager(keystore: String) {

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    private val keyStore = KeyStore.getInstance(keystore).apply {
        load(null)
    }

    private fun encryptCipher(secret: String): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey(secret))
        }
    }

    private fun getDecryptCipherForIv(storedIv: ByteArray, secret: String): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(secret), IvParameterSpec(storedIv))
        }
    }

    private fun getKey(secret: String): SecretKey {
        val existingKey = keyStore.getEntry(secret, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(secret)
    }

    private fun createKey(secret: String): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    secret,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(clearText: String, secret: String): String {
        val cipher = encryptCipher(secret)
        val cipherText =
            Base64.encodeToString(cipher.doFinal(clearText.toByteArray()), Base64.DEFAULT)
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

        return "$cipherText.$iv"
    }

    fun decrypt(cipherText: String, secret: String): String {
        val array = cipherText.split(".")
        val cipherData = Base64.decode(array.first(), Base64.DEFAULT)
        val iv = Base64.decode(array.last(), Base64.DEFAULT)

        val clearText = getDecryptCipherForIv(iv, secret).doFinal(cipherData)

        return String(clearText, 0, clearText.size, Charsets.UTF_8)
    }
}
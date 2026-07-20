package com.estoyok.app.core.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = encryptCipher
        val encryptedBytes = cipher.doFinal(bytes)
        // Prepend IV to the encrypted bytes so we can retrieve it during decryption
        return cipher.iv + encryptedBytes
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        if (bytes.size < 12) return ByteArray(0)
        val iv = bytes.copyOfRange(0, 12) // GCM IV is 12 bytes
        val encryptedBytes = bytes.copyOfRange(12, bytes.size)
        return getDecryptCipherForIv(iv).doFinal(encryptedBytes)
    }

    fun encryptString(plaintext: String): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decryptString(ciphertext: String): String {
        val bytes = Base64.decode(ciphertext, Base64.DEFAULT)
        return String(decrypt(bytes), Charsets.UTF_8)
    }

    companion object {
        private const val ALIAS = "estoyok_secret_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

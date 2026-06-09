package com.flashcards.app.data.auth

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return Base64.encodeToString(factory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun saltToString(salt: ByteArray): String = Base64.encodeToString(salt, Base64.NO_WRAP)

    fun saltFromString(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    fun verify(password: String, salt: ByteArray, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}

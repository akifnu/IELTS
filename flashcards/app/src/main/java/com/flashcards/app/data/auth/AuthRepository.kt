package com.flashcards.app.data.auth

import com.flashcards.app.data.ShineRepository
import com.flashcards.app.data.dao.UserDao
import com.flashcards.app.data.entity.UserEntity
import com.flashcards.app.domain.UserSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionStore: SessionStore,
    private val shineRepository: ShineRepository,
) {
    val session: Flow<UserSession> = sessionStore.session

    suspend fun continueAsGuest(displayName: String = "") {
        val name = displayName.trim().ifBlank { "Guest" }
        sessionStore.save(UserSession(id = "guest", name = name, provider = "guest"))
        shineRepository.updateUserName(name)
    }

    suspend fun registerWithEmail(name: String, email: String, password: String) {
        val em = email.trim().lowercase()
        require(em.isNotBlank() && password.length >= 8) { "Invalid credentials" }
        if (userDao.findByEmail(em) != null) error("Account already exists")
        val salt = PasswordHasher.newSalt()
        val user = UserEntity(
            id = "email_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}",
            email = em,
            name = name.trim().ifBlank { em.substringBefore("@") },
            provider = "email",
            passwordHash = PasswordHasher.hash(password, salt),
            salt = PasswordHasher.saltToString(salt),
        )
        userDao.upsert(user)
        signInUser(user)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        val em = email.trim().lowercase()
        val user = userDao.findByEmail(em) ?: error("No account found")
        val salt = PasswordHasher.saltFromString(user.salt.orEmpty())
        if (!PasswordHasher.verify(password, salt, user.passwordHash.orEmpty())) {
            error("Incorrect password")
        }
        signInUser(user)
    }

    suspend fun signInWithGoogle(id: String, email: String?, name: String?, avatar: String?) {
        val userId = "google_$id"
        val existing = userDao.findById(userId)
        val user = existing ?: UserEntity(
            id = userId,
            email = email.orEmpty(),
            name = name?.trim().orEmpty().ifBlank { "Google user" },
            provider = "google",
            avatar = avatar,
        )
        userDao.upsert(user)
        signInUser(user)
    }

    suspend fun signOut() {
        sessionStore.clear()
        sessionStore.save(UserSession())
    }

    private suspend fun signInUser(user: UserEntity) {
        sessionStore.save(
            UserSession(
                id = user.id,
                email = user.email.takeIf { it.isNotBlank() },
                name = user.name,
                provider = user.provider,
                avatar = user.avatar,
            ),
        )
        shineRepository.updateUserName(user.name)
    }
}

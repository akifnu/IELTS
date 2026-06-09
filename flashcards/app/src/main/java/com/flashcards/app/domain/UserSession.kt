package com.flashcards.app.domain

data class UserSession(
    val id: String = "guest",
    val email: String? = null,
    val name: String = "Guest",
    val provider: String = "guest",
    val avatar: String? = null,
) {
    val isGuest: Boolean get() = provider == "guest"
    val isSignedIn: Boolean get() = !isGuest
}

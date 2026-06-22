package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
        SessionProvider.saveUser(user)
    }

    fun loadSession() {
        _currentUser.value = SessionProvider.loadUser()
    }

    fun logout() {
        setUser(null)
        SessionProvider.clear()
    }
}

expect suspend fun loginWithLine(): User?

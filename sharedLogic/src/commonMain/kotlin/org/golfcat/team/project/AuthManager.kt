package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    // 💡 Wasm 專用的 Mock 使用者，避開 Serialization Bug
    private val mockUser = User(id = "u1", lineUid = "L1", realName = "測試員")
    private val _currentUser = MutableStateFlow<User?>(mockUser)
    val currentUser = _currentUser.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
    }

    fun loadSession() {
        _currentUser.value = mockUser
    }

    fun logout() {
        setUser(null)
    }
}

expect suspend fun loginWithLine(): User?

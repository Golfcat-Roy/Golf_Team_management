package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    // 💡 Mock User for Wasm/JS bypass
    private val mockUser = User(
        id = "u1", 
        lineUid = "L1", 
        lineDisplayName = "Tester", 
        realName = "測試管理員",
        initialHandicap = 18.0,
        isSuperAdmin = true,
        createdAt = null
    )

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

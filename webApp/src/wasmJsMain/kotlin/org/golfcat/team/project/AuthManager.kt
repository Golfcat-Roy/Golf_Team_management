package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private val mockUser = User(
        id = "u1", 
        lineUid = "L1", 
        lineDisplayName = "Tester", 
        realName = "Admin Tester", // 💡 英文化
        initialHandicap = 18.0,
        isSuperAdmin = true,
        createdAt = null
    )
    
    private val _currentUser = MutableStateFlow<User?>(mockUser)
    val currentUser = _currentUser.asStateFlow()
}

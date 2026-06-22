package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await

actual suspend fun loginWithLine(): User? {
    if (!liff.isLoggedIn()) {
        liff.login()
        return null
    }
    
    return try {
        val profile = liff.getProfile().await()
        User(
            lineUid = profile.userId,
            lineDisplayName = profile.displayName,
            realName = profile.displayName,
            initialHandicap = 36.0,
            isSuperAdmin = false
        )
    } catch (e: Exception) {
        println("LIFF getProfile error: $e")
        null
    }
}

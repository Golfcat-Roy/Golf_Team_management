package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlinx.browser.window

actual suspend fun loginWithLine(): User? {
    val liff = window.asDynamic().liff
    if (liff == null) {
        console.error("AuthManager: LIFF SDK not found")
        return null
    }
    
    if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
        console.log("AuthManager: User not logged in, calling liff.login()")
        liff.login()
        return null
    }
    
    return try {
        console.log("AuthManager: Fetching profile...")
        val profile = liff.getProfile().await().asDynamic()
        console.log("AuthManager: Profile fetched for ${profile.displayName}")
        User(
            lineUid = profile.userId as String,
            lineDisplayName = profile.displayName as String,
            realName = profile.displayName as String,
            initialHandicap = 36.0,
            isSuperAdmin = false
        )
    } catch (e: Exception) {
        console.error("AuthManager: Error fetching profile: ${e.message}")
        null
    }
}

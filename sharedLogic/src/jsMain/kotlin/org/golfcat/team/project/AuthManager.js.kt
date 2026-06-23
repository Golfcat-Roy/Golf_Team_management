package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlinx.browser.window

actual suspend fun loginWithLine(): User? {
    val liff = window.asDynamic().liff
    if (liff == null || !liff.isLoggedIn().unsafeCast<Boolean>()) {
        liff?.login()
        return null
    }
    
    return try {
        val profile = liff.getProfile().await().asDynamic()
        User(
            lineUid = profile.userId as String,
            lineDisplayName = profile.displayName as String,
            realName = profile.displayName as String,
            initialHandicap = 36.0,
            isSuperAdmin = false
        )
    } catch (e: Exception) {
        null
    }
}

package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlinx.browser.window

actual suspend fun loginWithLine(): User? {
    val liff = window.asDynamic().liff
    if (liff == null) {
        window.alert("錯誤: 找不到 LIFF SDK")
        return null
    }
    
    if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
        liff.login()
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
        window.alert("登入過程發生異常: " + e.message)
        null
    }
}

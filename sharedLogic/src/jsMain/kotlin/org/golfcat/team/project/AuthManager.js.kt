package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun loginWithLine(): User? {
    val liff = window.asDynamic().liff ?: return null
    
    if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
        liff.login()
        return null
    }
    
    return suspendCoroutine { continuation ->
        liff.getProfile().then({ profile: Any ->
            val p = profile.unsafeCast<dynamic>()
            continuation.resume(User(
                lineUid = p.userId.unsafeCast<String>(),
                lineDisplayName = p.displayName.unsafeCast<String>(),
                realName = p.displayName.unsafeCast<String>(),
                initialHandicap = 36.0,
                isSuperAdmin = false
            ))
        }, {
            continuation.resume(null)
        })
    }
}

package org.golfcat.team.project

import kotlinx.browser.window
import kotlinx.coroutines.await

class JsPlatform: Platform {
    override val name: String = "Web / LIFF"
}

actual fun getPlatform(): Platform = JsPlatform()

class JsPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? {
        val liff = window.asDynamic().liff
        return try {
            if (liff != null && liff.isLoggedIn().unsafeCast<Boolean>()) {
                liff.getProfile().await().asDynamic().userId as? String
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getPlatformService(): PlatformService = JsPlatformService()

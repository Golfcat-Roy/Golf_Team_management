package org.golfcat.team.project

import kotlinx.browser.window
import kotlinx.coroutines.await

class JsPlatform: Platform {
    override val name: String = "Web / LIFF"
}

actual fun getPlatform(): Platform = JsPlatform()

class JsPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? {
        return try {
            if (liff.isLoggedIn()) {
                liff.getProfile().await().userId
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

actual fun getPlatformService(): PlatformService = JsPlatformService()

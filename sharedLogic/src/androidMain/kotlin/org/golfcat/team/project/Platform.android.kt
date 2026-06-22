package org.golfcat.team.project

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

class AndroidPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? = null // TODO: Implement for Android
}

actual fun getPlatformService(): PlatformService = AndroidPlatformService()

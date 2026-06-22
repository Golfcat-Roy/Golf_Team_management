package org.golfcat.team.project

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

class IOSPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? = null // TODO: Implement for iOS
}

actual fun getPlatformService(): PlatformService = IOSPlatformService()

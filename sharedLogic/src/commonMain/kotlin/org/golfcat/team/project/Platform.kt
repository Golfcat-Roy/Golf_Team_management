package org.golfcat.team.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface PlatformService {
    suspend fun getLineUserId(): String?
}

expect fun getPlatformService(): PlatformService

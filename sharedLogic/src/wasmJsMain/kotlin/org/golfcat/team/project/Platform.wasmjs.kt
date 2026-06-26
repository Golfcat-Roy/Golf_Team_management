package org.golfcat.team.project

class WasmPlatform: Platform {
    override val name: String = "Web (Wasm)"
}

actual fun getPlatform(): Platform = WasmPlatform()

class WasmPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? = null
}

actual fun getPlatformService(): PlatformService = WasmPlatformService()

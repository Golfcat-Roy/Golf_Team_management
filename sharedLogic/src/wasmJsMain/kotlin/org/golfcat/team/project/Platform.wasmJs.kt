package org.golfcat.team.project

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

external fun getLineUserIdFromJS(): String?

class WasmPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? = getLineUserIdFromJS()
}

actual fun getPlatformService(): PlatformService = WasmPlatformService()

@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package org.golfcat.team.project

import kotlinx.coroutines.await
import kotlin.js.Promise

class WasmPlatform: Platform {
    override val name: String = "Web / LIFF (Wasm)"
}

actual fun getPlatform(): Platform = WasmPlatform()

private fun isLiffAvailable(): Boolean = js("typeof window.liff !== 'undefined'")
private fun isLiffLoggedIn(): Boolean = js("window.liff.isLoggedIn()")
private fun getLiffUserIdAsync(): Promise<JsString> = js("window.liff.getProfile().then(p => p.userId)")


class WasmPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? {
        return try {
            if (isLiffAvailable() && isLiffLoggedIn()) {
                getLiffUserIdAsync().await<JsString>().toString()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Wasm getLineUserId failed: ${e.message}")
            null
        }
    }
}

actual fun getPlatformService(): PlatformService = WasmPlatformService()
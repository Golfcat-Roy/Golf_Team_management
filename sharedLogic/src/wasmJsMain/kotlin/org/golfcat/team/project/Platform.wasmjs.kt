package org.golfcat.team.project

import kotlinx.coroutines.await
import kotlin.js.Promise

// 💡 1. 重新命名類別，清晰標示這是 Wasm 平台
class WasmPlatform: Platform {
    override val name: String = "Web / LIFF (Wasm)"
}

actual fun getPlatform(): Platform = WasmPlatform()

// 💡 2. 使用 js() 封裝純 JavaScript 操作
private fun isLiffAvailable(): Boolean = js("typeof window.liff !== 'undefined'")
private fun isLiffLoggedIn(): Boolean = js("window.liff.isLoggedIn()")

// 💡 這裡直接在 JS 端把 userId 拿出來，返回 Promise<JsString>，非常優雅
private fun getLiffUserIdAsync(): Promise<JsString> = js("window.liff.getProfile().then(p => p.userId)")

class WasmPlatformService : PlatformService {
    override suspend fun getLineUserId(): String? {
        return try {
            // 💡 3. 安全地檢查 LIFF 狀態
            if (isLiffAvailable() && isLiffLoggedIn()) {
                // 💡 4. 等待 Promise 完成，並將 JsString 轉回 Kotlin 的 String
                getLiffUserIdAsync().await().toString()
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
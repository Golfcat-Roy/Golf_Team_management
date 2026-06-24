package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlin.js.Promise

// 💡 1. 嚴格定義外部 JavaScript 物件的結構 (必須繼承 JsAny)
external interface LiffProfile : JsAny {
    val userId: JsString
    val displayName: JsString
}

// 💡 2. 使用 Kotlin/Wasm 專屬的 js() 語法來安全橋接瀏覽器全域變數
private fun isLiffAvailable(): Boolean = js("typeof window.liff !== 'undefined'")
private fun isLiffLoggedIn(): Boolean = js("window.liff.isLoggedIn()")
private fun liffLogin(): Unit = js("window.liff.login()")
private fun getLiffProfile(): Promise<LiffProfile> = js("window.liff.getProfile()")

actual suspend fun loginWithLine(): User? {
    // 保護機制：確保網頁上有掛載 Line LIFF SDK
    if (!isLiffAvailable()) {
        println("Error: LIFF SDK not found on window.")
        return null
    }

    if (!isLiffLoggedIn()) {
        liffLogin() // 如果未登入，呼叫 LIFF 的登入畫面
        return null
    }

    return try {
        // 💡 3. 安全地等待 JavaScript Promise 完成，並取得我們定義好的 LiffProfile
        val profile = getLiffProfile().await()

        User(
            // 💡 4. 將 Wasm 的 JsString 轉換回 Kotlin 原生的 String
            lineUid = profile.userId.toString(),
            lineDisplayName = profile.displayName.toString(),
            realName = profile.displayName.toString(),
            initialHandicap = 36.0,
            isSuperAdmin = false
        )
    } catch (e: Exception) {
        println("LIFF getProfile failed: ${e.message}")
        null
    }
}
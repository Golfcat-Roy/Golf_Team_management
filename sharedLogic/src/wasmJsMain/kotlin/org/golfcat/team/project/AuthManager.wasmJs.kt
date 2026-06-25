@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlinx.browser.window

external interface LiffProfile : JsAny {
    val userId: JsString
    val displayName: JsString
}

private fun isLiffAvailable(): Boolean = js("typeof window.liff !== 'undefined'")
private fun isLiffLoggedIn(): Boolean = js("window.liff.isLoggedIn()")
private fun liffLogin(): Unit = js("window.liff.login()")
private fun getLiffProfile(): Promise<LiffProfile> = js("window.liff.getProfile()")
private fun liffInit(liffId: String): Promise<JsAny?> = js("window.liff.init({ liffId: liffId })")

// 💡 開發模式：如果是 localhost 則自動使用 Mock User
private fun isDevelopmentMode(): Boolean = window.location.hostname == "localhost" || window.location.hostname == "127.0.0.1"

suspend fun initializeLiff(liffId: String) {
    if (isDevelopmentMode()) {
        println("Development Mode: Skipping LIFF initialization.")
        return
    }

    if (!isLiffAvailable()) {
        println("Error: LIFF SDK not found on window.")
        return
    }
    try {
        liffInit(liffId).await<JsAny?>()
        println("LIFF init success")
    } catch (e: Exception) {
        println("LIFF init failed: ${e.message}")
    }
}

actual suspend fun loginWithLine(): User? {
    if (isDevelopmentMode()) {
        println("Development Mode: Returning Mock User.")
        return User(
            id = "mock-user-id",
            lineUid = "U1234567890",
            lineDisplayName = "測試開發員",
            realName = "測試開發員",
            initialHandicap = 36.0,
            isSuperAdmin = true
        )
    }

    if (!isLiffAvailable()) {
        println("Error: LIFF SDK not found on window.")
        return null
    }

    if (!isLiffLoggedIn()) {
        liffLogin()
        return null
    }


    return try {
        val profile = getLiffProfile().await<LiffProfile>()
        User(
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

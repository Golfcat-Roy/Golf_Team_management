@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.coroutines.await
import kotlin.js.Promise

external interface LiffProfile : JsAny {
    val userId: JsString
    val displayName: JsString
}

private fun isLiffAvailable(): Boolean = js("typeof window.liff !== 'undefined'")
private fun isLiffLoggedIn(): Boolean = js("window.liff.isLoggedIn()")
private fun liffLogin(): Unit = js("window.liff.login()")
private fun getLiffProfile(): Promise<LiffProfile> = js("window.liff.getProfile()")

actual suspend fun loginWithLine(): User? {
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
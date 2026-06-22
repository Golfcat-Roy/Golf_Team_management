package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.browser.window

actual suspend fun loginWithLine(): User? {
    val liff = window.asDynamic().liff
    if (liff == null) return null
    
    if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
        liff.login()
        return null
    }
    
    // 改用 Promise 避免 await() 崩潰
    // 註：這是一個 suspend 函式，我們需要手動處理續行 (Continuation)
    // 但最簡單的方法是讓 UI 層去處理回傳值，這裡我們先暫時回傳一個空 User 並由 main.kt 處理
    return null
}

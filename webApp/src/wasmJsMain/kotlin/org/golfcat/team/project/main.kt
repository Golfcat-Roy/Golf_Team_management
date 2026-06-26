package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        println("Starting Wasm Application...")
        ComposeViewport(document.body!!) {
            App()
        }
        println("Application started successfully.")
    } catch (e: Throwable) {
        // 💡 如果啟動失敗，直接彈出警告，讓我們知道錯誤訊息
        val msg = "Startup Error: ${e.message}"
        println(msg)
        window.alert(msg)
    }
}

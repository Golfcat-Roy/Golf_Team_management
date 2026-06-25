package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

// 💡 如果 App() 還是亮紅燈，請把前面的註解拿掉，或是用 Alt+Enter 匯入
// import org.golfcat.team.project.ui.screens.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 取得網頁的 body 元素
    val body = document.body ?: return

    // 💡 拋棄找不到的 CanvasBasedWindow，改用最新標準的 ComposeViewport
    ComposeViewport(body) {
        App()
    }
}
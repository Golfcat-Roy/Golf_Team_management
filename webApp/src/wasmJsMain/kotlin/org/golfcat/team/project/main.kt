package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

// 這裡不需要 import renderComposable，那是給純網頁開發用的

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "Golf Team Management",
        canvasElementId = "ComposeTarget" // 這個 ID 會對應到 index.html 的 canvas
    ) {
        // 呼叫你的主畫面起點
        App()
    }
}
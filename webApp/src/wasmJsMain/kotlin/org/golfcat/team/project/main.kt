package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.golfcat.team.project.sharedUI.App // 確保有正確引用到你的共享 UI

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 綁定到 index.html 中的 canvas id
    CanvasBasedWindow(canvasId = "ComposeTarget") {
        App()
    }
}
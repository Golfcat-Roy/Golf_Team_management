package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

// 💡 這裡必須匯入你 sharedUI 裡面真正的「UI 進入點」函數
// 從你剛才的編譯日誌看來，你的主畫面應該是 MainAppShell
import org.golfcat.team.project.ui.screens.MainAppShell

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // CanvasBasedWindow 是 Wasm 網頁版的專屬容器
    CanvasBasedWindow(
        title = "Golf Team Management",
        canvasElementId = "ComposeTarget" // 這個 ID 必須跟你網頁 index.html 裡的 canvas id 一致
    ) {
        // 在這裡呼叫你的主畫面
        MainAppShell()
    }
}
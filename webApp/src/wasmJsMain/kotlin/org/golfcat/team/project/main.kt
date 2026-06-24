package org.golfcat.team.project

// 💡 確保 Compose Web 的 Window 啟動器有被正確匯入
import androidx.compose.ui.ExperimentalComposeUiApi
import org.golfcat.team.project.App
import androidx.compose.web.renderComposable
// 💡 因為 App() 通常就定義在 org.golfcat.team.project 這個 package 底下，
// 所以通常不需要額外 import，直接呼叫即可！

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeEntryPoint(
        title = "Golf Team Management",
        canvasElementId = "ComposeTarget" // 請確認 index.html 裡的 canvas id 也是這個
    ) {
        // 💡 呼叫我們剛剛找到的真正起點
        App()
    }
}
package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return

    MainScope().launch {
        // 初始化 LIFF (使用你的 LIFF ID)
        initializeLiff("2006760548-o1V9b80z")

        ComposeViewport(body) {
            App()
        }
    }
}

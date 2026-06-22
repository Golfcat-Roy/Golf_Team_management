package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
    val liffId = "2010382913-rCaKoQcE"
    
    fun launchApp() {
        val root = document.getElementById("app-root")
        if (root != null) {
            ComposeViewport(root) {
                App()
            }
        }
    }

    if (liff == null || liff == undefined) {
        launchApp()
        return
    }

    // 只做初始化，不手動設定 User，讓 LoginScreen 的邏輯來跑
    liff.init(kotlin.js.json("liffId" to liffId)).then({
        launchApp()
    }, { 
        launchApp()
    })
}

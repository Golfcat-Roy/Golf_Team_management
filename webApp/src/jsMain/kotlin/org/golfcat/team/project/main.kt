package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
    
    if (liff == null || liff == undefined) {
        startComposeApp()
        return
    }

    val liffId = "2010382913-rCaKoQcE"
    
    // 使用更簡單的對象定義
    val initConfig = kotlin.js.json("liffId" to liffId)
    
    liff.init(initConfig).then({
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            startComposeApp()
        }
    }, { err ->
        startComposeApp()
    })
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startComposeApp() {
    // 確保 body 存在後再載入
    val body = document.body
    if (body != null) {
        ComposeViewport(body) {
            App()
        }
    }
}

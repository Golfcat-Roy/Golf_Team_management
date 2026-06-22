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

    // 啟動 LIFF 並且直接在啟動時檢查
    liff.init(kotlin.js.json("liffId" to liffId)).then({
        val isLoggedIn = liff.isLoggedIn().unsafeCast<Boolean>()
        if (!isLoggedIn) {
            liff.login()
        } else {
            launchApp()
        }
    }, { err ->
        window.alert("LIFF 錯誤: " + err)
        launchApp()
    })
}

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
    
    // 增加超時保護，確保就算 LIFF 失敗也能進入程式
    val timeout = window.setTimeout({
        startComposeApp()
    }, 3000)

    liff.init(kotlin.js.json("liffId" to liffId)).then({
        window.clearTimeout(timeout)
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            startComposeApp()
        }
    }, { err ->
        window.clearTimeout(timeout)
        console.error("LIFF Error", err)
        startComposeApp()
    })
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startComposeApp() {
    val body = document.body
    if (body != null) {
        // 解決亂碼：確保 Canvas 渲染區域是乾淨的
        body.innerHTML = "<div id='app-root' style='width:100%; height:100%;'></div>"
        ComposeViewport(document.getElementById("app-root")!!) {
            App()
        }
    }
}

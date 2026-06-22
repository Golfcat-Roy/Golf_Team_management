package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
    
    // 初始化時先移除 loading 狀態
    val liffId = "2010382913-rCaKoQcE"
    
    if (liff == null || liff == undefined) {
        startComposeApp()
        return
    }

    liff.init(kotlin.js.json("liffId" to liffId)).then({
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            startComposeApp()
        }
    }, { err ->
        console.error("LIFF Init Error", err)
        startComposeApp()
    })
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startComposeApp() {
    val container = document.getElementById("compose-target") as? HTMLDivElement
    if (container != null) {
        ComposeViewport(container) {
            App()
        }
    } else {
        // Fallback to body
        document.body?.let {
            ComposeViewport(it) {
                App()
            }
        }
    }
}

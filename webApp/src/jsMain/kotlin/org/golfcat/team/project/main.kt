package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
    
    if (liff == null) {
        startComposeApp()
        return
    }

    val liffId = "2010382913-rCaKoQcE" // 您的 LIFF ID
    
    val initParams = js("{}")
    initParams.liffId = liffId
    
    liff.init(initParams).then({
        if (!liff.isLoggedIn()) {
            liff.login()
        } else {
            startComposeApp()
        }
    }, { err ->
        console.error("LIFF 初始化失敗", err)
        startComposeApp()
    })
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startComposeApp() {
    ComposeViewport {
        App()
    }
}

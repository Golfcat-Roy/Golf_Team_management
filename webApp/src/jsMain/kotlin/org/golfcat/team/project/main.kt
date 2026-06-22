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
    
    val initConfig = kotlin.js.json("liffId" to liffId)
    
    liff.init(initConfig).then({
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
    val body = document.body
    if (body != null) {
        ComposeViewport(body) {
            App()
        }
    }
}

package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
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
    ComposeViewport(window.document.body!!) {
        App()
    }
}

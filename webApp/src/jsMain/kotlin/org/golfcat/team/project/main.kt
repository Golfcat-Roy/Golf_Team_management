package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    console.log("App starting...")
    val liff = window.asDynamic().liff
    val liffId = "2010382913-rCaKoQcE"
    
    fun launchApp() {
        console.log("Launching Compose App...")
        val root = document.getElementById("app-root")
        if (root != null) {
            ComposeViewport(root) {
                App()
            }
        } else {
            console.error("Root element not found!")
        }
    }

    if (liff == null || liff == undefined) {
        console.warn("LIFF SDK not found, launching in standalone mode")
        launchApp()
        return
    }

    console.log("Initializing LIFF with ID: $liffId")
    liff.init(kotlin.js.json("liffId" to liffId)).then({
        console.log("LIFF initialized successfully")
        val isLoggedIn = liff.isLoggedIn().unsafeCast<Boolean>()
        console.log("Is logged in: $isLoggedIn")
        
        if (!isLoggedIn) {
            console.log("Not logged in, triggering liff.login()")
            liff.login()
        } else {
            launchApp()
        }
    }, { err ->
        val errorMsg = "LIFF Init Error: ${JSON.stringify(err)}"
        console.error(errorMsg)
        window.alert(errorMsg)
        launchApp()
    })
}

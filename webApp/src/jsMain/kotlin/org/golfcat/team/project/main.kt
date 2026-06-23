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
            while (root.hasChildNodes()) {
                root.removeChild(root.firstChild!!)
            }
            ComposeViewport(root) {
                App()
            }
        }
    }

    if (liff == null || liff == undefined) {
        launchApp()
        return
    }

    liff.init(kotlin.js.json("liffId" to liffId)).then({
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            // 在啟動前，我們先等待字體加載完成，這能有效減少亂碼機率
            document.asDynamic().fonts.ready.then({
                launchApp()
            })
        }
    }, {
        launchApp()
    })
}

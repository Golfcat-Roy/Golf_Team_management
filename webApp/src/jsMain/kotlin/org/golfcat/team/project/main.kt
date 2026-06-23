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
            // 清空舊節點，避免重複掛載導致的 Unspecified type 錯誤
            while (root.hasChildNodes()) { root.removeChild(root.firstChild!!) }
            ComposeViewport(root) { App() }
        }
    }

    if (liff == null || liff == undefined) {
        launchApp()
        return
    }

    // 延遲初始化，避開瀏覽器啟動時的資源爭奪 (減少 AbortError)
    window.setTimeout({
        liff.init(kotlin.js.json("liffId" to liffId)).then({
            if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
                liff.login()
            } else {
                launchApp()
            }
        }, { launchApp() })
    }, 100)
}

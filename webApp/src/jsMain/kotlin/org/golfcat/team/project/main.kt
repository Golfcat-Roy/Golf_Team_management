package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val liff = window.asDynamic().liff
    val liffId = "2010382913-rCaKoQcE"
    
    fun safeStart() {
        val root = document.getElementById("app-root")
        if (root != null) {
            ComposeViewport(root) {
                App()
            }
        }
    }

    if (liff == null || liff == undefined) {
        safeStart()
        return
    }

    // 嘗試初始化 LIFF
    liff.init(kotlin.js.json("liffId" to liffId)).then({
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            safeStart()
        }
    }, { err ->
        // 如果 LIFF 出錯，跳出訊息並強行開啟程式，方便排錯
        window.alert("LIFF 載入失敗: " + err.toString())
        safeStart()
    })
}

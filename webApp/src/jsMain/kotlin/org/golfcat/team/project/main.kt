package org.golfcat.team.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.golfcat.team.project.models.User

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

    liff.init(kotlin.js.json("liffId" to liffId)).then({
        if (!liff.isLoggedIn().unsafeCast<Boolean>()) {
            liff.login()
        } else {
            // 在進入 Compose 之前就先拿好 Profile，避開 Kotlin 的 await() 錯誤
            liff.getProfile().then({ profile ->
                val p = profile.asDynamic()
                AuthManager.setUser(User(
                    lineUid = p.userId as String,
                    lineDisplayName = p.displayName as String,
                    realName = p.displayName as String,
                    initialHandicap = 36.0,
                    isSuperAdmin = false
                ))
                launchApp()
            }, { 
                launchApp() 
            })
        }
    }, { err ->
        launchApp()
    })
}

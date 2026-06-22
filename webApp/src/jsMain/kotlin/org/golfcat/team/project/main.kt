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
            // 使用最安全的 unsafeCast 避免 asDynamic 崩潰
            liff.getProfile().then({ profile: Any ->
                val p = profile.unsafeCast<dynamic>()
                AuthManager.setUser(User(
                    lineUid = p.userId.unsafeCast<String>(),
                    lineDisplayName = p.displayName.unsafeCast<String>(),
                    realName = p.displayName.unsafeCast<String>(),
                    initialHandicap = 36.0,
                    isSuperAdmin = false
                ))
                launchApp()
            }, { 
                launchApp() 
            })
        }
    }, { err: Any ->
        console.error("LIFF Init Error", err)
        launchApp()
    })
}

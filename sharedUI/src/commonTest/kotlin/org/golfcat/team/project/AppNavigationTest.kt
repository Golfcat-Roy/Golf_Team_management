package org.golfcat.team.project

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.golfcat.team.project.models.User
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavigationTest {

    @BeforeTest
    fun setup() {
        AuthManager.logout()
    }

    @Test
    fun testLoginScreenShownWhenNotLoggedIn() = runComposeUiTest {
        setContent {
            App()
        }
        
        // Check for welcoming text in LoginScreen
        onNodeWithText("歡迎來到 GolfCat 團隊管理", substring = true).assertExists()
        onNodeWithText("使用 LINE 登入").assertExists()
    }

    @Test
    fun testProfileSetupShownWhenRealNameIsDefault() = runComposeUiTest {
        // Set user with default name "User"
        val newUser = User(id = "u1", lineUid = "L1", realName = "User")
        AuthManager.setUser(newUser)
        
        setContent {
            App()
        }
        
        // Should show ProfileSetupScreen
        onNodeWithText("個人資料設定").assertExists()
    }
}

package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.AuthManager
import org.golfcat.team.project.TeamRepository
import org.golfcat.team.project.loginWithLine
import org.golfcat.team.project.ui.components.GCButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val scope = rememberCoroutineScope()
    val repository = androidx.compose.runtime.remember { TeamRepository() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("歡迎來到 GolfCat 團隊管理", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
            GCButton(
                onClick = {
                    scope.launch {
                        val user = loginWithLine()
                        if (user != null) {
                            val syncedUser = repository.syncUser(user)
                            AuthManager.setUser(syncedUser)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF00B900))
            ) {
                // 加入英文，確保字體失效時仍能辨識按鈕
                Text("LINE LOGIN (使用 LINE 登入)", color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

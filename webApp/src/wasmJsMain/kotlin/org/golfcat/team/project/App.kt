package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.models.*

@Composable
fun App() {
    val currentUser by AuthManager.currentUser.collectAsState()
    
    val events = listOf(
        EventWithDetails(id = "e1", title = "2024 夏季賽", date = "2024-06-25", location = "大溪球場", registrationStatus = "closed", handicapRule = "New_New_Peoria", startTime = "08:00", groupCount = 4, participantCount = 16, isUserRegistered = true, isArchivedInList = false)
    )

    // 💡 定義一個支援中文的字體家族 (嘗試讓系統回退)
    val chineseTypography = Typography(
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif)
    )

    MaterialTheme(typography = chineseTypography) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (currentUser == null) {
                Box(contentAlignment = Alignment.Center) {
                    Text("請先登入")
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "歡迎回來, ${currentUser?.realName ?: "未知用戶"}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "今日賽事列表 (Monolith Mock)", style = MaterialTheme.typography.titleLarge)
                    
                    events.forEach { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = "地點: ${event.location ?: "未定"}")
                                Text(text = "日期: ${event.date}")
                            }
                        }
                    }
                }
            }
        }
    }
}

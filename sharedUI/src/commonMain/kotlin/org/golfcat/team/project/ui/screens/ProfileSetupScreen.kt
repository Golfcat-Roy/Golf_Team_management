package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(user: User, onBack: (() -> Unit)? = null) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    val currentUser by AuthManager.currentUser.collectAsState()
    
    var realName by remember { mutableStateOf(user.realName) }
    var handicap by remember { mutableStateOf(user.initialHandicap.toString()) }
    var joinCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLeaveConfirm by remember { mutableStateOf<Team?>(null) }
    
    var joinedTeams by remember { mutableStateOf<List<Team>>(emptyList()) }

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            joinedTeams = repository.getMyTeams(userId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("個人資料設定", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = realName,
            onValueChange = { realName = it },
            label = { Text("真實姓名") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = handicap,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) handicap = it },
            label = { Text("初始差點 (預設 36.0)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = joinCode,
            onValueChange = { joinCode = it },
            label = { Text("加入新球隊 (輸入邀請碼)") },
            placeholder = { Text("請輸入球隊邀請碼") },
            modifier = Modifier.fillMaxWidth()
        )
        
        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(Modifier.height(32.dp))
        
        GCButton(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val userId = user.id ?: throw Exception("User ID missing")
                        val updatedUser = repository.updateUserProfile(userId, realName, handicap.toDoubleOrNull() ?: 36.0)
                        if (joinCode.isNotBlank()) {
                            repository.joinTeamByCode(joinCode, userId, handicap.toDoubleOrNull() ?: 36.0)
                        }
                        AuthManager.setUser(updatedUser)
                        onBack?.invoke()
                    } catch (e: Exception) {
                        errorMessage = "設定失敗: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && realName.isNotBlank() && realName != "User"
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
            else Text("儲存並套用")
        }
        
        if (joinedTeams.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("目前所屬球隊", style = MaterialTheme.typography.titleMedium)
            joinedTeams.forEach { team ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(team.name)
                    GCTextButton(onClick = { showLeaveConfirm = team }) {
                        Text("退出", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        GCTextButton(onClick = { AuthManager.logout() }) {
            Text("登出帳號", color = MaterialTheme.colorScheme.error)
        }
        
        onBack?.let {
            GCTextButton(onClick = it) {
                Text("返回首頁")
            }
        }
    }

    showLeaveConfirm?.let { team ->
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = null },
            title = { Text("確認退出球隊") },
            text = { Text("您確定要退出「${team.name}」嗎？退出後需重新輸入邀請碼才能加入。") },
            confirmButton = {
                GCButton(
                    onClick = {
                        scope.launch {
                            try {
                                val userId = user.id ?: return@launch
                                val teamId = team.id ?: return@launch
                                val member = repository.getMember(userId, teamId)
                                val memberId = member?.id
                                if (memberId != null) {
                                    repository.deleteTeamMember(memberId)
                                    joinedTeams = repository.getMyTeams(userId)
                                }
                                showLeaveConfirm = null
                            } catch (e: Exception) {
                                errorMessage = "退出失敗: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確認退出")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { showLeaveConfirm = null }) { Text("取消") }
            }
        )
    }
}

package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun TeamManagementTab(
    teamId: String, 
    teamStatus: String = "active", 
    subscriptionType: String = "free",
    memberLimit: Int = 30,
    currentSubTab: String,
    onSubTabChange: (String) -> Unit,
    memberSubTab: Int,
    onMemberSubTabChange: (Int) -> Unit,
    onNavigateToCreateEvent: (String, String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    val currentUser by AuthManager.currentUser.collectAsState()
    var memberRole by remember { mutableStateOf("member") }
    var members by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingHandicapMemberId by remember { mutableStateOf<String?>(null) }
    var editingRoleMemberId by remember { mutableStateOf<String?>(null) }
    var newHandicapInput by remember { mutableStateOf("") }

    var memberToDelete by remember { mutableStateOf<MemberWithUser?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val userId = currentUser?.id ?: return@launch
                memberRole = repository.getMember(userId, teamId)?.role ?: "member"
                members = repository.getLeaderboard(teamId)
            } catch (e: Exception) {
                println("TeamManagementTab load error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(teamId, currentUser) {
        loadData()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (!MemberRoles.isAdmin(memberRole)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("存取受限", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Text("只有球隊總幹事可以存取此管理頁面。", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            when (currentSubTab) {
                "members" -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                        GCButton(
                            onClick = { onSubTabChange("menu") },
                            shape = androidx.compose.foundation.shape.CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Back", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        
                        Text(
                            "成員管理", 
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    TabRow(selectedTabIndex = memberSubTab, modifier = Modifier.padding(bottom = 16.dp)) {
                        Tab(selected = memberSubTab == 0, onClick = { onMemberSubTabChange(0) }, text = { Text("正式球員") })
                        Tab(selected = memberSubTab == 1, onClick = { onMemberSubTabChange(1) }, text = { Text("來賓名單") })
                    }
                    
                    val filteredMembers = if (memberSubTab == 0) {
                        members.filter { it.role != "guest" }
                    } else {
                        members.filter { it.role == "guest" }.distinctBy { it.users.realName }
                    }

                    LazyColumn(Modifier.weight(1f)) {
                        items(filteredMembers, key = { it.id }) { member ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                member.users.realName, 
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            val infoText = if (member.role == "guest") "角色: 來賓" else "職務: ${MemberRoles.getDisplayName(member.role)}"
                                            Text(
                                                "$infoText | 差點: ${member.handicap}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        if (editingHandicapMemberId == member.id) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedTextField(
                                                    value = newHandicapInput,
                                                    onValueChange = { newHandicapInput = it },
                                                    modifier = Modifier.width(70.dp),
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodySmall
                                                )
                                                GCIconButton(onClick = {
                                                    scope.launch {
                                                        val h = newHandicapInput.toDoubleOrNull() ?: member.handicap
                                                        repository.updateMemberHandicap(member.id, h)
                                                        editingHandicapMemberId = null
                                                        loadData()
                                                    }
                                                }) { Text("✔", color = MaterialTheme.colorScheme.primary) }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (editingHandicapMemberId != member.id) {
                                            GCElevatedCard(
                                                onClick = {
                                                    editingHandicapMemberId = member.id
                                                    newHandicapInput = member.handicap.toString()
                                                },
                                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                    Text("修改差點", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }

                                        if (member.role != "guest") {
                                            Box(modifier = Modifier.weight(1f)) {
                                                GCElevatedCard(
                                                    onClick = { editingRoleMemberId = member.id },
                                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                        Text("變更職務", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = editingRoleMemberId == member.id,
                                                    onDismissRequest = { editingRoleMemberId = null }
                                                ) {
                                                    listOf(MemberRoles.PRESIDENT, MemberRoles.VICE_PRESIDENT, MemberRoles.ADMIN, MemberRoles.VICE_ADMIN, MemberRoles.MEMBER).forEach { r ->
                                                        DropdownMenuItem(
                                                            text = { Text(MemberRoles.getDisplayName(r)) },
                                                            onClick = {
                                                                scope.launch {
                                                                    repository.updateMemberRole(member.id, r)
                                                                    editingRoleMemberId = null
                                                                    loadData()
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        GCElevatedCard(
                                            onClick = {
                                                memberToDelete = member
                                                deleteErrorMessage = null
                                            },
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    if (member.role == "guest") "移除來賓" else "移除成員",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text("球隊管理面板", style = MaterialTheme.typography.headlineSmall)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (subscriptionType == "pro") MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("目前方案", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (subscriptionType == "pro") "Pro 專業版" else "Free 免費版",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (subscriptionType == "pro") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            )
                            
                            Spacer(Modifier.width(20.dp))

                            Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👥 ", style = MaterialTheme.typography.bodyMedium)
                                    Text("成員上限: $memberLimit 人", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📊 ", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = if (subscriptionType == "pro") "歷史紀錄: 無限制" else "歷史紀錄: 最近 2 場",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    if (teamStatus == "suspended") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text("此球隊目前已被系統暫停使用，無法建立新賽事。如有疑問請聯絡管理員。", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }

                    GCButton(
                        onClick = { onNavigateToCreateEvent(teamId, null) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        enabled = teamStatus != "suspended"
                    ) {
                        if (teamStatus == "suspended") {
                            Text("建立新賽事 (停用中)")
                        } else {
                            Text("建立新賽事")
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    GCButton(
                        onClick = { onSubTabChange("members") },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("成員管理 (差點/權限)")
                    }
                }
            }
        }
    }

    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("確認移除成員") },
            text = {
                Column {
                    val isSelf = member.users.id == currentUser?.id
                    val isAdminRole = MemberRoles.isAdmin(member.role)
                    val adminCount = members.count { MemberRoles.isAdmin(it.role) }

                    if (isSelf && isAdminRole && adminCount <= 1) {
                        Text("您是球隊中唯一的管理員，無法移除自己。請先指派其他成員為管理職務。", color = MaterialTheme.colorScheme.error)
                    } else if (isSelf) {
                        Text("您正在嘗試移除**您自己**。移除後您將立即失去管理權限，且必須重新輸入邀請碼才能加入球隊。確定要繼續嗎？")
                    } else {
                        Text("確定要將成員 ${member.users.realName} 從球隊中移除嗎？此動作無法復原。")
                    }
                    
                    deleteErrorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                val isSelf = member.users.id == currentUser?.id
                val isAdminRole = MemberRoles.isAdmin(member.role)
                val adminCount = members.count { MemberRoles.isAdmin(it.role) }
                val canDelete = !(isSelf && isAdminRole && adminCount <= 1)

                GCButton(
                    onClick = {
                        scope.launch {
                            try {
                                repository.deleteTeamMember(member.id)
                                memberToDelete = null
                                loadData()
                                if (isSelf) {
                                    AuthManager.logout() 
                                }
                            } catch (e: Exception) {
                                deleteErrorMessage = "刪除失敗: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = canDelete
                ) {
                    Text("確認移除")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { memberToDelete = null }) { Text("取消") }
            }
        )
    }
}

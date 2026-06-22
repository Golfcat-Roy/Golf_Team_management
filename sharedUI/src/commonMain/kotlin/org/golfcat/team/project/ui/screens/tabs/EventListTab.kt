package org.golfcat.team.project.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
fun EventListTab(
    teamId: String,
    onNavigateToScoring: (String, String) -> Unit,
    onNavigateToGrouping: (String, String?) -> Unit,
    onNavigateToLeaderboard: (String) -> Unit,
    onNavigateToCreateEvent: (String, String?) -> Unit,
    onNavigateToDuplicateEvent: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    var events by remember { mutableStateOf<List<EventWithDetails>>(emptyList()) }
    var memberRole by remember { mutableStateOf("member") }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val currentUser by AuthManager.currentUser.collectAsState()

    var showParticipantsDialog by remember { mutableStateOf<String?>(null) }
    var showGuestDialog by remember { mutableStateOf<String?>(null) }
    var showCancelConfirmDialog by remember { mutableStateOf<EventWithDetails?>(null) }
    var showArchiveConfirmDialog by remember { mutableStateOf<EventWithDetails?>(null) }
    var showVotingDialog by remember { mutableStateOf<EventWithDetails?>(null) }
    var showGuestCancelConfirmDialog by remember { mutableStateOf<Pair<String, MemberWithUser>?>(null) }
    var participants by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }
    var guestNameInput by remember { mutableStateOf("") }
    var guestHandicapInput by remember { mutableStateOf("36.0") }

    fun loadData() {
        scope.launch {
            isLoading = true
            loadError = null
            try {
                val userId = currentUser?.id ?: return@launch
                events = repository.getEventsWithDetails(teamId, userId)
                memberRole = repository.getMember(userId, teamId)?.role ?: "member"
            } catch (e: Exception) {
                println("EventListTab loadData error: ${e.message}")
                loadError = e.message ?: "載入失敗"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(teamId, currentUser) {
        loadData()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (loadError != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("載入資料失敗: $loadError", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                GCButton(onClick = { loadData() }) {
                    Text("重試")
                }
            }
        }
    } else if (events.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("目前沒有即將到來的球賽喔！")
        }
    } else {
        val isAdmin = MemberRoles.isAdmin(memberRole) || currentUser?.isSuperAdmin == true
        val firstNotClosedIndex = events.indexOfFirst { it.registrationStatus != "closed" }

        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                val isClosed = event.registrationStatus == "closed"
                val isNearest = index == firstNotClosedIndex
                val isStarted = event.registrationStatus == "started"
                
                val cardColor = when {
                    isClosed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    isNearest -> androidx.compose.ui.graphics.Color(0xFFFAFAFA)
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val borderColor = if (isNearest) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isNearest) 6.dp else if (isClosed) 2.dp else 4.dp
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isNearest) 2.dp else 1.dp, 
                        color = borderColor
                    ),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        val formattedDate = event.date.replace("-", ".")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${event.title} - $formattedDate", 
                                style = MaterialTheme.typography.titleLarge, 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        val infoTextStyle = MaterialTheme.typography.titleMedium
                        val ruleName = when(event.handicapRule) {
                            "Team_Handicap" -> "球隊差點"
                            "New_New_Peoria" -> "新新貝利亞"
                            "Match_Play" -> "分組對抗"
                            else -> event.handicapRule
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth()) {
                                Text("地點: ${event.location ?: "未指定"}", modifier = Modifier.weight(1f), style = infoTextStyle)
                                Text("時間: ${event.startTime ?: "未指定"}", modifier = Modifier.weight(1f), style = infoTextStyle)
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Text("賽制: $ruleName", modifier = Modifier.weight(1f), style = infoTextStyle)
                                Text("組數: ${event.groupCount ?: 0} 組", modifier = Modifier.weight(1f), style = infoTextStyle)
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        if (!isClosed) {
                            if (!isStarted) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (event.isUserRegistered) {
                                        Column(Modifier.weight(1f)) {
                                            GCButton(
                                                onClick = { showCancelConfirmDialog = event },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.fillMaxWidth()
                                            ) { Text("取消報名", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                            
                                            if (event.handicapRule == "New_New_Peoria") {
                                                Spacer(Modifier.height(4.dp))
                                                GCOutlinedButton(
                                                    onClick = { showVotingDialog = event },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) { Text("變更隱藏洞投票", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                            }
                                        }
                                    } else {
                                        var regError by remember { mutableStateOf<String?>(null) }
                                        Column(Modifier.weight(1f)) {
                                            GCButton(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            val userId = currentUser?.id ?: return@launch
                                                            val m = repository.getMember(userId, teamId) ?: return@launch
                                                            repository.registerForEvent(event.id, m.id!!)
                                                            if (event.handicapRule == "New_New_Peoria") {
                                                                showVotingDialog = event
                                                            }
                                                            loadData()
                                                        } catch (e: Exception) {
                                                            regError = e.message ?: "報名失敗"
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) { Text("我要報名", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                            
                                            regError?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                    
                                    GCButton(
                                        onClick = { showGuestDialog = event.id },
                                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("來賓報名", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GCButton(
                                        onClick = { showParticipantsDialog = event.id },
                                        modifier = if (isAdmin) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) { Text("參賽名單 (${event.participantCount})", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                    
                                    if (isAdmin) {
                                        if (event.handicapRule == "Match_Play") {
                                            Row(Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                GCOutlinedButton(
                                                    onClick = { onNavigateToGrouping(event.id, "A") },
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("A隊-分組", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                                GCOutlinedButton(
                                                    onClick = { onNavigateToGrouping(event.id, "B") },
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("B隊-分組", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                            }
                                        } else {
                                            GCOutlinedButton(
                                                onClick = { onNavigateToGrouping(event.id, null) },
                                                modifier = Modifier.weight(1f)
                                            ) { Text("分組設定", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                        }
                                    }
                                }
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GCButton(
                                        onClick = { showParticipantsDialog = event.id },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) { Text("參賽名單 (${event.participantCount})", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }

                                    GCButton(
                                        onClick = { onNavigateToLeaderboard(event.id) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) { Text("即時排行", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                }

                                Spacer(Modifier.height(8.dp))

                                GCButton(
                                    onClick = {
                                        scope.launch {
                                            val userId = currentUser?.id ?: return@launch
                                            val groupId = repository.getUserGroupId(userId, event.id)
                                            if (groupId != null) {
                                                onNavigateToScoring(event.id, groupId)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = event.isUserRegistered
                                ) {
                                    Text("輸入成績", style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f, fontWeight = FontWeight.Bold))
                                }
                            }
                        } else {
                            GCButton(
                                onClick = { onNavigateToLeaderboard(event.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("查看最終成績 🏆", style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f, fontWeight = FontWeight.Bold))
                            }
                        }

                        if (isAdmin) {
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Text("管理員功能", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            
                            var adminError by remember { mutableStateOf<String?>(null) }
                            if (adminError != null) {
                                Text(adminError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                            }

                            if (!isClosed) {
                                if (event.handicapRule == "Match_Play" && !isStarted) {
                                    GCButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    adminError = null
                                                    repository.pairGroupsRandomly(event.id)
                                                    loadData()
                                                } catch (e: Exception) {
                                                    adminError = "配對失敗: ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Text("自動配對", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold))
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GCButton(
                                            onClick = { onNavigateToCreateEvent(teamId, event.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp),
                                            enabled = !isStarted
                                        ) { Text("變更資訊", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }

                                        GCButton(
                                            onClick = { onNavigateToDuplicateEvent(teamId, event.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) { Text("複製賽事", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                    }

                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val actionText = if (isStarted) "結束球賽" else "開始球賽"
                                        val actionColor = if (isStarted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        GCButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        adminError = null
                                                        if (isStarted) repository.finishEvent(event.id) else repository.startEvent(event.id)
                                                        loadData()
                                                    } catch (e: Exception) {
                                                        adminError = "操作失敗: ${e.message}"
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) { Text(actionText, style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }

                                        GCButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        adminError = null
                                                        repository.deleteEvent(event.id)
                                                        loadData()
                                                    } catch (e: Exception) {
                                                        adminError = "刪除失敗: ${e.message}"
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp),
                                            enabled = !isStarted
                                        ) { Text("刪除賽事", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold)) }
                                    }
                                }
                            } else {
                                GCButton(
                                    onClick = { showArchiveConfirmDialog = event },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("從列表移除 (不刪除歷史紀錄)", style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showParticipantsDialog != null) {
        val eventId = showParticipantsDialog ?: ""
        val currentEvent = events.find { it.id == eventId }
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        LaunchedEffect(eventId) {
            participants = repository.getEventParticipants(eventId)
        }
        AlertDialog(
            onDismissRequest = { showParticipantsDialog = null },
            title = { Text("參賽名單") },
            text = {
                Column {
                    if (participants.isNotEmpty()) {
                        GCButton(
                            onClick = {
                                val header = "⛳ 【${currentEvent?.title ?: "球賽"}】參賽名單\n" +
                                        "📅 日期: ${currentEvent?.date ?: ""}\n" +
                                        "⏰ 時間: ${currentEvent?.startTime ?: "未指定"}\n" +
                                        "📍 地點: ${currentEvent?.location ?: "未指定"}\n\n"
                                
                                val list = participants.mapIndexed { index, p ->
                                    val groupSideText = if (p.groupSide != null) "${p.groupSide}隊-" else ""
                                    val groupText = if (p.groupNumber != null) "【$groupSideText 第 ${p.groupNumber} 組】" else "【未分組】"
                                    "${index + 1}. $groupText ${p.users.realName}"
                                }.joinToString("\n")
                                
                                val footer = "\n\n👥 目前報名人數：${participants.size} 人"
                                
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(header + list + footer))
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("複製報名清單 (貼至 LINE)")
                        }
                    }

                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(participants) { p ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val groupSideText = if (p.groupSide != null) "${p.groupSide}隊-" else ""
                                    val groupText = if (p.groupNumber != null) "【$groupSideText 第 ${p.groupNumber} 組】 " else "【未分組】 "
                                    val vsText = if (p.pairedGroupName != null) " vs ${p.pairedGroupName}" else ""

                                    Text(groupText + vsText, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    Text(p.users.realName)
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("HCP: ${p.handicap}", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
                                    
                                    if (MemberRoles.isAdmin(memberRole) || p.role == "guest") {
                                        GCIconButton(
                                            onClick = { showGuestCancelConfirmDialog = eventId to p },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("✕", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { GCTextButton(onClick = { showParticipantsDialog = null }) { Text("關閉") } }
        )
    }

    if (showGuestDialog != null) {
        val eventId = showGuestDialog ?: ""
        AlertDialog(
            onDismissRequest = { showGuestDialog = null },
            title = { Text("來賓報名") },
            text = {
                Column {
                    OutlinedTextField(value = guestNameInput, onValueChange = { guestNameInput = it }, label = { Text("來賓姓名") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = guestHandicapInput, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) guestHandicapInput = it }, label = { Text("初始差點") })
                }
            },
            confirmButton = {
                var isSubmitting by remember { mutableStateOf(false) }
                var errorMsg by remember { mutableStateOf<String?>(null) }
                
                Column(horizontalAlignment = Alignment.End) {
                    errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
                    GCButton(onClick = {
                        scope.launch {
                            isSubmitting = true
                            errorMsg = null
                            try {
                                val userId = currentUser?.id ?: return@launch
                                val m = repository.getMember(userId, teamId) ?: return@launch
                                val memberId = m.id ?: return@launch
                                repository.registerGuest(eventId, memberId, guestNameInput, guestHandicapInput.toDoubleOrNull() ?: 36.0)
                                showGuestDialog = null
                                loadData()
                            } catch (e: Exception) {
                                errorMsg = e.message ?: "報名失敗"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }, enabled = guestNameInput.isNotBlank() && !isSubmitting) { 
                        if (isSubmitting) CircularProgressIndicator(Modifier.size(16.dp)) else Text("確認報名") 
                    }
                }
            },
            dismissButton = { GCTextButton(onClick = { showGuestDialog = null }) { Text("取消") } }
        )
    }

    if (showCancelConfirmDialog != null) {
        val event = showCancelConfirmDialog ?: return
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = null },
            title = { Text("確認取消報名") },
            text = { Text("您確定要取消報名「${event.title}」嗎？") },
            confirmButton = {
                GCButton(
                    onClick = {
                        scope.launch {
                            val userId = currentUser?.id ?: return@launch
                            val m = repository.getMember(userId, teamId) ?: return@launch
                            val memberId = m.id ?: return@launch
                            repository.cancelRegistration(event.id, memberId)
                            showCancelConfirmDialog = null
                            loadData()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確認取消")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { showCancelConfirmDialog = null }) {
                    Text("先不要")
                }
            }
        )
    }

    if (showArchiveConfirmDialog != null) {
        val event = showArchiveConfirmDialog ?: return
        AlertDialog(
            onDismissRequest = { showArchiveConfirmDialog = null },
            title = { Text("確認從列表移除") },
            text = { Text("這會將「${event.title}」從目前的賽事列表中移除，但會完整保留在「歷史成績」中。確定要繼續嗎？") },
            confirmButton = {
                GCButton(
                    onClick = {
                        scope.launch {
                            repository.archiveEventFromList(event.id)
                            showArchiveConfirmDialog = null
                            loadData()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("確認移除")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { showArchiveConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showGuestCancelConfirmDialog != null) {
        val (eventId, member) = showGuestCancelConfirmDialog ?: return
        AlertDialog(
            onDismissRequest = { showGuestCancelConfirmDialog = null },
            title = { Text("確認移除人員") },
            text = { Text("確定要將「${member.users.realName}」從參賽名單中移除嗎？") },
            confirmButton = {
                GCButton(
                    onClick = {
                        scope.launch {
                            repository.cancelRegistration(eventId, member.id)
                            showGuestCancelConfirmDialog = null
                            loadData()
                            participants = repository.getEventParticipants(eventId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確認移除")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { showGuestCancelConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showVotingDialog != null) {
        val event = showVotingDialog ?: return
        var selectedHoles by remember { mutableStateOf(setOf<Int>()) }
        
        AlertDialog(
            onDismissRequest = { showVotingDialog = null },
            title = { Text("新新貝利亞隱藏洞投票") },
            text = {
                Column {
                    Text("請選擇您心目中的 6 個隱藏洞 (目前已選: ${selectedHoles.size}/6)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..18).forEach { hole ->
                            GCFilterChip(
                                selected = selectedHoles.contains(hole),
                                onClick = {
                                    if (selectedHoles.contains(hole)) {
                                        selectedHoles = selectedHoles - hole
                                    } else if (selectedHoles.size < 6) {
                                        selectedHoles = selectedHoles + hole
                                    }
                                },
                                label = { Text("$hole") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                GCButton(
                    onClick = {
                        scope.launch {
                            val userId = currentUser?.id ?: return@launch
                            val m = repository.getMember(userId, teamId) ?: return@launch
                            val memberId = m.id ?: return@launch
                            repository.submitVotes(event.id, memberId, selectedHoles.toList())
                            showVotingDialog = null
                        }
                    },
                    enabled = selectedHoles.size == 6
                ) {
                    Text("確認投票")
                }
            },
            dismissButton = {
                GCTextButton(onClick = { showVotingDialog = null }) {
                    Text("稍後再投")
                }
            }
        )
    }
}

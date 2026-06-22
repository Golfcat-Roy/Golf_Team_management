package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupingScreen(eventId: String, side: String? = null, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    var groupsWithMembers by remember { mutableStateOf<List<EventGroupWithMembers>>(emptyList()) }
    var participants by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    var groupErrorMessage by remember { mutableStateOf<String?>(null) }
    var isInitializing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Draft state for batch editing
    var draftGroups by remember { mutableStateOf<List<EventGroupWithMembers>>(emptyList()) }
    var hasChanges by remember { mutableStateOf(false) }

    suspend fun loadData(showLoading: Boolean = false) {
        if (showLoading) isLoading = true
        groupErrorMessage = null
        try {
            val user = AuthManager.currentUser.value
            val event = repository.getEventById(eventId)
            val teamId = event?.teamId ?: ""
            
            if (user?.id != null) {
                val member = repository.getMember(user.id!!, teamId)
                isAdmin = user.isSuperAdmin || MemberRoles.isAdmin(member?.role)
            }
            
            val count = event?.groupCount ?: 0
            var currentGroups = repository.getGroupsWithMembers(eventId, side)
            
            if (isAdmin && count > 0 && currentGroups.size < count && !isInitializing) {
                isInitializing = true
                try {
                    val existingNums = currentGroups.map { it.group.groupNumber }.toSet()
                    (1..count).forEach { num ->
                        if (num !in existingNums) {
                            repository.createEventGroup(eventId, num, side)
                        }
                    }
                    currentGroups = repository.getGroupsWithMembers(eventId, side).sortedBy { it.group.groupNumber }
                } finally {
                    isInitializing = false
                }
            }
            
            groupsWithMembers = currentGroups
            participants = repository.getEventParticipants(eventId)
            draftGroups = currentGroups
            hasChanges = false
        } catch (e: Exception) {
            groupErrorMessage = "載入失敗: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(eventId) { loadData(showLoading = true) }

    fun saveChanges() {
        scope.launch {
            isSaving = true
            try {
                val assignments = draftGroups.flatMap { groupData ->
                    groupData.members.map { member ->
                        groupData.group.id!! to member.id
                    }
                }
                repository.batchUpdateGroupMembers(eventId, side, assignments)
                loadData(showLoading = true)
                hasChanges = false
            } catch (e: Exception) {
                groupErrorMessage = "儲存失敗: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { 
                    val titleText = when(side) {
                        "A" -> "A隊 - 分組設定"
                        "B" -> "B隊 - 分組設定"
                        else -> "賽事分組設定"
                    }
                    Text(titleText, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) 
                },
                navigationIcon = {
                    GCButton(
                        onClick = {
                            if (hasChanges) {
                                // Could add a confirmation dialog here
                                onBack()
                            } else {
                                onBack()
                            }
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Back", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    if (isAdmin && hasChanges) {
                        GCTextButton(
                            onClick = { saveChanges() },
                            enabled = !isSaving,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("儲存變更", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val cardModifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        val cardColors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
        val cardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        val cardBorder = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!isAdmin && !isLoading) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "唯讀模式：僅管理員可修改分組",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            if (groupErrorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(groupErrorMessage!!, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                val assignedMemberIds = draftGroups.flatMap { it.members.map { m -> m.id } }.toSet()
                val unassignedMembers = participants.filter { it.id !in assignedMemberIds }

                LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                    if (unassignedMembers.isNotEmpty()) {
                        item {
                            Text("未分組成員", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                        }
                        items(unassignedMembers, key = { it.id }) { member ->
                            var isExpanded by remember { mutableStateOf(false) }
                            Card(modifier = cardModifier, colors = cardColors, elevation = cardElevation, border = cardBorder) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(member.users.realName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    if (isAdmin) {
                                        Box {
                                            GCButton(
                                                onClick = { isExpanded = true },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text("指派組別 ▼", style = MaterialTheme.typography.labelMedium)
                                            }
                                            DropdownMenu(
                                                expanded = isExpanded,
                                                onDismissRequest = { isExpanded = false }
                                            ) {
                                                draftGroups.forEach { groupData ->
                                                    val isFull = groupData.members.size >= 4 && side == null // Regular event usually 4 per group
                                                    // In match play (side != null), usually 2 per group
                                                    val isMatchFull = groupData.members.size >= 2 && side != null
                                                    
                                                    val fullStatus = isMatchFull || isFull
                                                    
                                                    DropdownMenuItem(
                                                        text = { 
                                                            val fullText = if (fullStatus) " (已滿)" else ""
                                                            Text("第 ${groupData.group.groupNumber} 組$fullText") 
                                                        },
                                                        onClick = {
                                                            isExpanded = false
                                                            // Local Update
                                                            draftGroups = draftGroups.map { g ->
                                                                if (g.group.id == groupData.group.id) {
                                                                    g.copy(members = g.members + member)
                                                                } else g
                                                            }
                                                            hasChanges = true
                                                        },
                                                        enabled = !fullStatus
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }

                    item {
                        Text("已分組名單", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                    }

                    items(draftGroups, key = { it.group.id!! }) { groupData ->
                        Card(
                            modifier = cardModifier, 
                            colors = cardColors, 
                            elevation = cardElevation, 
                            border = cardBorder
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("第 ${groupData.group.groupNumber} 組", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    
                                    if (isAdmin && groupData.members.isNotEmpty()) {
                                        GCTextButton(
                                            onClick = {
                                                // Local Update: Clear members
                                                draftGroups = draftGroups.map { g ->
                                                    if (g.group.id == groupData.group.id) {
                                                        g.copy(members = emptyList())
                                                    } else g
                                                }
                                                hasChanges = true
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) { 
                                            Text("清空名單", style = MaterialTheme.typography.labelMedium) 
                                        }
                                    }
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                if (groupData.members.isEmpty()) {
                                    Text("尚未指派成員", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                } else {
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        groupData.members.forEach { member ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(member.users.realName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                                    if (isAdmin) {
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            "✕", 
                                                            modifier = Modifier.clickable {
                                                                // Local Update: Remove member
                                                                draftGroups = draftGroups.map { g ->
                                                                    if (g.group.id == groupData.group.id) {
                                                                        g.copy(members = g.members.filter { it.id != member.id })
                                                                    } else g
                                                                }
                                                                hasChanges = true
                                                            },
                                                            color = MaterialTheme.colorScheme.error,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

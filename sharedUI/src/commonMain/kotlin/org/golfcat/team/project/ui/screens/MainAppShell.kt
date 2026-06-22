package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import org.golfcat.team.project.ui.screens.tabs.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    selectedTeam: Team?,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    historySubTab: Int,
    onHistorySubTabChange: (Int) -> Unit,
    managementSubTab: String,
    onManagementSubTabChange: (String) -> Unit,
    memberManagementSubTab: Int,
    onMemberManagementSubTabChange: (Int) -> Unit,
    onTeamSelected: (Team) -> Unit,
    onNavigateToScoring: (String, String) -> Unit,
    onNavigateToCreateEvent: (String, String?) -> Unit,
    onNavigateToDuplicateEvent: (String, String) -> Unit,
    onNavigateToSuperAdmin: () -> Unit,
    onNavigateToGrouping: (String, String?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: (String) -> Unit
) {
    val repository = remember { TeamRepository() }
    val currentUser by AuthManager.currentUser.collectAsState()
    var joinedTeams by remember { mutableStateOf<List<Team>>(emptyList()) }
    var isTeamMenuExpanded by remember { mutableStateOf(false) }
    val tabs = listOf("賽事列表", "排行榜", "歷史成績", "球隊管理", "使用說明")

    val isSuperAdmin = currentUser?.isSuperAdmin == true

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            val updatedTeams = repository.getMyTeams(userId)
            joinedTeams = updatedTeams
            if (updatedTeams.isNotEmpty()) {
                if (selectedTeam == null) {
                    onTeamSelected(updatedTeams.first())
                } else {
                    updatedTeams.find { it.id == selectedTeam.id }?.let {
                        onTeamSelected(it)
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (isSuperAdmin) {
                        GCIconButton(onClick = onNavigateToSuperAdmin) {
                            Text("🛡️", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isTeamMenuExpanded = true }
                    ) {
                        Text(selectedTeam?.name ?: "選擇球隊", style = MaterialTheme.typography.titleLarge)
                        GCIconButton(onClick = { isTeamMenuExpanded = true }) {
                            Text("▼")
                        }
                        DropdownMenu(
                            expanded = isTeamMenuExpanded,
                            onDismissRequest = { isTeamMenuExpanded = false }
                        ) {
                            joinedTeams.forEach { team ->
                                DropdownMenuItem(
                                    text = { Text(team.name) },
                                    onClick = {
                                        onTeamSelected(team)
                                        isTeamMenuExpanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("加入/退出球隊") },
                                onClick = {
                                    isTeamMenuExpanded = false
                                    onNavigateToProfile()
                                }
                            )
                        }
                    }
                },
                actions = {
                    GCIconButton(onClick = onNavigateToProfile) {
                        Text("👤")
                    }
                    GCTextButton(onClick = { AuthManager.logout() }) {
                        Text("登出", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { onTabChange(index) },
                            label = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.2f,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) 
                            },
                            icon = { /* Icon placeholder */ },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                indicatorColor = androidx.compose.ui.graphics.Color(0xFF00E676)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            selectedTeam?.let { team ->
                val teamId = team.id ?: ""
                when (selectedTab) {
                    0 -> EventListTab(teamId, onNavigateToScoring, onNavigateToGrouping, onNavigateToLeaderboard, onNavigateToCreateEvent, onNavigateToDuplicateEvent)
                    1 -> LeaderboardTab(teamId)
                    2 -> HistoryTab(teamId, team.subscriptionType, historySubTab, onHistorySubTabChange, onNavigateToLeaderboard)
                    3 -> TeamManagementTab(teamId, team.subscriptionStatus, team.subscriptionType, team.memberLimit, managementSubTab, onManagementSubTabChange, memberManagementSubTab, onMemberManagementSubTabChange, onNavigateToCreateEvent)
                    4 -> InstructionTab()
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("您尚未加入任何球隊")
                    Spacer(Modifier.height(16.dp))
                    GCButton(onClick = { onNavigateToProfile() }) {
                        Text("前往加入/建立球隊")
                    }
                }
            }
        }
    }
}

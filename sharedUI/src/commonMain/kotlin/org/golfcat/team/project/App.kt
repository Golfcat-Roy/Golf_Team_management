package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import org.golfcat.team.project.ui.screens.*

@Composable
fun App(repository: TeamRepository = remember { TeamRepository() }) {
    val currentUser by AuthManager.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf("main") }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var selectedDuplicateFromId by remember { mutableStateOf<String?>(null) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var selectedTeamId by remember { mutableStateOf<String?>(null) }
    var selectedSide by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(0) }
    var historySubTab by remember { mutableStateOf(0) }
    var managementSubTab by remember { mutableStateOf("menu") }
    var memberManagementSubTab by remember { mutableStateOf(0) }
    var leaderboardSource by remember { mutableStateOf("main") }

    var hasTeams by remember { mutableStateOf<Boolean?>(null) }
    var isSessionLoaded by remember { mutableStateOf(false) }
    
    var activeTeam by remember { mutableStateOf<Team?>(null) }

    LaunchedEffect(Unit) {
        AuthManager.loadSession()
        isSessionLoaded = true
    }

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            val userId = user.id
            if (userId != null) {
                try {
                    val teams = repository.getMyTeams(userId)
                    hasTeams = teams.isNotEmpty()
                } catch (e: Exception) {
                    if (e.message?.contains("406") == true || e.message?.contains("not found") == true) {
                        AuthManager.logout()
                    }
                    hasTeams = false
                }
            } else {
                hasTeams = false
            }
        } else {
            hasTeams = null
            activeTeam = null
        }
    }

    AppContent(
        currentUser = currentUser,
        isSessionLoaded = isSessionLoaded,
        hasTeams = hasTeams,
        currentScreen = currentScreen,
        onScreenChange = { currentScreen = it },
        selectedTab = selectedTab,
        onTabChange = { selectedTab = it },
        historySubTab = historySubTab,
        onHistorySubTabChange = { historySubTab = it },
        managementSubTab = managementSubTab,
        onManagementSubTabChange = { managementSubTab = it },
        memberManagementSubTab = memberManagementSubTab,
        onMemberManagementSubTabChange = { memberManagementSubTab = it },
        selectedEventId = selectedEventId,
        onEventSelected = { selectedEventId = it },
        selectedDuplicateFromId = selectedDuplicateFromId,
        onDuplicateFromSelected = { selectedDuplicateFromId = it },
        selectedGroupId = selectedGroupId,
        onGroupSelected = { selectedGroupId = it },
        selectedTeamId = selectedTeamId,
        onTeamIdSelected = { selectedTeamId = it },
        selectedSide = selectedSide,
        onSideSelected = { selectedSide = it },
        activeTeam = activeTeam,
        onActiveTeamChange = { activeTeam = it },
        leaderboardSource = leaderboardSource,
        onLeaderboardSourceChange = { leaderboardSource = it },
        repository = repository
    )
}

@Composable
fun AppContent(
    currentUser: User?,
    isSessionLoaded: Boolean,
    hasTeams: Boolean?,
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    historySubTab: Int,
    onHistorySubTabChange: (Int) -> Unit,
    managementSubTab: String,
    onManagementSubTabChange: (String) -> Unit,
    memberManagementSubTab: Int,
    onMemberManagementSubTabChange: (Int) -> Unit,
    selectedEventId: String?,
    onEventSelected: (String?) -> Unit,
    selectedDuplicateFromId: String?,
    onDuplicateFromSelected: (String?) -> Unit,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit,
    selectedTeamId: String?,
    onTeamIdSelected: (String?) -> Unit,
    selectedSide: String?,
    onSideSelected: (String?) -> Unit,
    activeTeam: Team?,
    onActiveTeamChange: (Team?) -> Unit,
    leaderboardSource: String,
    onLeaderboardSourceChange: (String) -> Unit,
    repository: TeamRepository
) {
    val golfCatColorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF133B2B), // 經典深邃綠
        onPrimary = androidx.compose.ui.graphics.Color(0xFFFAFAFA), // 舒緩高亮白 (文字)
        primaryContainer = androidx.compose.ui.graphics.Color(0xFFE8F5E9), // 極淺草地綠 (容器背景)
        onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF133B2B),
        secondary = androidx.compose.ui.graphics.Color(0xFF2E7D32), // 活力草地綠
        onSecondary = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
        tertiary = androidx.compose.ui.graphics.Color(0xFF2E7D32),
        background = androidx.compose.ui.graphics.Color(0xFFFAFAFA), // 整體背景
        surface = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
        onBackground = androidx.compose.ui.graphics.Color(0xFF1A1A1A), // 炭墨黑 (文字)
        onSurface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
        error = androidx.compose.ui.graphics.Color(0xFFD32F2F), // 警示紅
        outline = androidx.compose.ui.graphics.Color(0xFF1A1A1A).copy(alpha = 0.6f)
    )

    val typography = Typography(
        bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif),
        bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif),
        titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif),
        labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
    )

    MaterialTheme(colorScheme = golfCatColorScheme, typography = typography) {
        val user = currentUser
        
        if (!isSessionLoaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Loading Session...", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else if (user == null) {
            LoginScreen()
        } else if (user.id == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Syncing User (Missing ID)...", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    GCButton(onClick = { AuthManager.logout() }) {
                        Text("Reset Login")
                    }
                }
            }
        } else if (hasTeams == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Checking Teams (hasTeams is null)...", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    GCButton(onClick = { AuthManager.logout() }) {
                        Text("Reset Login")
                    }
                }
            }
        } else if (user.realName == "User" || hasTeams == false) {
            ProfileSetupScreen(user)
        } else {
            when (currentScreen) {
                "scoring" -> ScoringScreen(
                    selectedEventId ?: "", 
                    selectedGroupId ?: "",
                    onNavigateToLeaderboard = {
                        onLeaderboardSourceChange("scoring")
                        onScreenChange("leaderboard")
                    }
                ) { onScreenChange("main") }
                "create_event" -> CreateEventScreen(selectedTeamId ?: "", selectedEventId, selectedDuplicateFromId) { 
                    onEventSelected(null)
                    onDuplicateFromSelected(null)
                    onScreenChange("main") 
                }
                "super_admin" -> SuperAdminScreen { onScreenChange("main") }
                "grouping" -> GroupingScreen(selectedEventId ?: "", selectedSide) { 
                    onSideSelected(null)
                    onScreenChange("main") 
                }
                "profile" -> ProfileSetupScreen(user) { onScreenChange("main") }
                "leaderboard" -> EventLeaderboardScreen(
                    eventId = selectedEventId ?: "",
                    isFromScoring = leaderboardSource == "scoring"
                ) { 
                    if (leaderboardSource == "scoring") onScreenChange("scoring")
                    else onScreenChange("main")
                }
                else -> MainAppShell(
                    selectedTeam = activeTeam,
                    selectedTab = selectedTab,
                    onTabChange = onTabChange,
                    historySubTab = historySubTab,
                    onHistorySubTabChange = onHistorySubTabChange,
                    managementSubTab = managementSubTab,
                    onManagementSubTabChange = onManagementSubTabChange,
                    memberManagementSubTab = memberManagementSubTab,
                    onMemberManagementSubTabChange = onMemberManagementSubTabChange,
                    onTeamSelected = { onActiveTeamChange(it) },
                    onNavigateToScoring = { eventId, groupId ->
                        onEventSelected(eventId)
                        onGroupSelected(groupId)
                        onScreenChange("scoring")
                    },
                    onNavigateToCreateEvent = { teamId, eventId ->
                        onTeamIdSelected(teamId)
                        onEventSelected(eventId)
                        onDuplicateFromSelected(null)
                        onScreenChange("create_event")
                    },
                    onNavigateToDuplicateEvent = { teamId, eventId ->
                        onTeamIdSelected(teamId)
                        onEventSelected(null)
                        onDuplicateFromSelected(eventId)
                        onScreenChange("create_event")
                    },
                    onNavigateToSuperAdmin = {
                        onScreenChange("super_admin")
                    },
                    onNavigateToGrouping = { eventId, side ->
                        onEventSelected(eventId)
                        onSideSelected(side)
                        onScreenChange("grouping")
                    },
                    onNavigateToProfile = {
                        onScreenChange("profile")
                    },
                    onNavigateToLeaderboard = { eventId ->
                        onEventSelected(eventId)
                        onLeaderboardSourceChange("main")
                        onScreenChange("leaderboard")
                    }
                )
            }
        }
    }
}

package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    var teamStats by remember { mutableStateOf<List<TeamStats>>(emptyList()) }
    var errorLogs by remember { mutableStateOf<List<AppErrorLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 球隊, 1: 錯誤日誌
    var showCreateDialog by remember { mutableStateOf(false) }
    var teamToDelete by remember { mutableStateOf<TeamStats?>(null) }
    
    var newTeamName by remember { mutableStateOf("") }
    var newJoinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try { 
                if (selectedTab == 0) {
                    teamStats = repository.getSuperAdminDashboard()
                } else {
                    errorLogs = repository.getErrorLogs()
                }
            } catch (e: Exception) {
                errorMessage = "載入失敗: ${e.message}"
            }
            finally { isLoading = false }
        }
    }

    LaunchedEffect(selectedTab) { loadData() }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    title = { Text("🛡️ 超級管理後台", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = { 
                        GCButton(
                            onClick = onBack,
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
                        if (selectedTab == 0) {
                            GCIconButton(onClick = { 
                                newTeamName = ""
                                newJoinCode = ""
                                showCreateDialog = true 
                            }) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        } else {
                            GCButton(
                                onClick = { loadData() },
                                shape = androidx.compose.foundation.shape.CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Refresh", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("球隊清單") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("錯誤異常訊息") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("壓力測試") })
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }

                when (selectedTab) {
                    0 -> TeamListSubTab(teamStats, repository, { loadData() }, { stats -> teamToDelete = stats })
                    1 -> ErrorLogSubTab(errorLogs)
                    2 -> MonteCarloTestSubTab(repository)
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("建立新球隊") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTeamName,
                            onValueChange = { newTeamName = it },
                            label = { Text("球隊名稱") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newJoinCode,
                            onValueChange = { newJoinCode = it },
                            label = { Text("自定義邀請碼 (Join Code)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    var isSubmitting by remember { mutableStateOf(false) }
                    GCButton(onClick = {
                        scope.launch {
                            isSubmitting = true
                            errorMessage = null
                            try {
                                repository.createTeamBySuperAdmin(newTeamName, newJoinCode)
                                showCreateDialog = false
                                loadData()
                            } catch (e: Exception) {
                                errorMessage = "建立失敗: ${e.message}"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }, enabled = newTeamName.isNotBlank() && newJoinCode.isNotBlank() && !isSubmitting) {
                        if (isSubmitting) CircularProgressIndicator(Modifier.size(20.dp))
                        else Text("建立")
                    }
                },
                dismissButton = {
                    GCTextButton(onClick = { showCreateDialog = false }) { Text("取消") }
                }
            )
        }

        teamToDelete?.let { team ->
            AlertDialog(
                onDismissRequest = { teamToDelete = null },
                title = { Text("⚠ 確認刪除球隊") },
                text = { Text("您確定要刪除「${team.teamName}」嗎？此操作將會永久移除該球隊所有成員、賽事及成績紀錄，且無法復原。") },
                confirmButton = {
                    GCButton(
                        onClick = {
                            scope.launch {
                                try {
                                    errorMessage = null
                                    repository.deleteTeam(team.teamId)
                                    teamToDelete = null
                                    loadData()
                                } catch (e: Exception) {
                                    errorMessage = "刪除失敗: ${e.message}"
                                    teamToDelete = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("確認永久刪除")
                    }
                },
                dismissButton = {
                    GCTextButton(onClick = { teamToDelete = null }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
fun TeamListSubTab(
    teamStats: List<TeamStats>,
    repository: TeamRepository,
    onRefresh: () -> Unit,
    onDeleteRequest: (TeamStats) -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn {
        items(teamStats) { team ->
            var editLimit by remember(team.teamId) { mutableStateOf(team.memberLimit.toString()) }
            var isPro by remember(team.teamId) { mutableStateOf(team.subscriptionType == "pro") }
            var isSaving by remember { mutableStateOf(false) }

            Card(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(team.teamName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isPro) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (isPro) "PRO 版" else "免費版",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPro) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (team.status == "active") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (team.status == "active") "使用中" else "暫停中",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (team.status == "active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("邀請碼: ${team.joinCode}", style = MaterialTheme.typography.bodyMedium)
                    Text("成員數: ${team.memberCount} / 上限: ${team.memberLimit} | 管理員: ${team.adminName ?: "尚未綁定"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    
                    Spacer(Modifier.height(16.dp))

                    // 權限調整區
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("升級 Pro", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = isPro, 
                                onCheckedChange = { isPro = it },
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                        
                        OutlinedTextField(
                            value = editLimit,
                            onValueChange = { if (it.all { c -> c.isDigit() }) editLimit = it },
                            label = { Text("人數上限") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            enabled = isPro // 免費版固定 30，Pro 版可改
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        GCButton(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    try {
                                        repository.updateTeamSubscription(team.teamId, if (isPro) "pro" else "free", editLimit.toIntOrNull() ?: 30)
                                        onRefresh()
                                    } catch (e: Exception) {
                                        println("Error updating subscription: ${e.message}")
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (isSaving) CircularProgressIndicator(Modifier.size(16.dp))
                            else Text("儲存權限", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GCButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val newStatus = if (team.status == "active") "suspended" else "active"
                                        repository.updateTeamStatus(team.teamId, newStatus)
                                        onRefresh()
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = if (team.status == "active") ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (team.status == "active") "暫停使用" else "恢復啟用", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        GCButton(
                            onClick = { onDeleteRequest(team) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                        ) {
                            Text("刪除球隊", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorLogSubTab(logs: List<AppErrorLog>) {
    if (logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("目前尚無異常紀錄")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                log.createdAt?.take(16)?.replace("T", " ") ?: "未知時間",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            log.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonteCarloTestSubTab(repository: TeamRepository) {
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<String?>(null) }
    var apiResults by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0f) }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text("邏輯壓力測試 (蒙地卡羅)", style = MaterialTheme.typography.titleLarge)
            Text("模擬 10,000 場比賽的隨機數據以驗證計算穩定性。", style = MaterialTheme.typography.bodySmall)
            
            Spacer(Modifier.height(16.dp))
            
            GCButton(
                onClick = {
                    scope.launch {
                        isRunning = true
                        results = null
                        val startTime = Clock.System.now().toEpochMilliseconds()
                        var totalErrors = 0
                        val iterations = 10000
                        
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                            for (i in 1..iterations) {
                                try {
                                    runMonteCarloIteration()
                                    if (i % 100 == 0) progress = i / iterations.toFloat()
                                } catch (e: Exception) {
                                    totalErrors++
                                }
                            }
                        }
                        
                        val endTime = Clock.System.now().toEpochMilliseconds()
                        val duration = endTime - startTime
                        val avg = duration.toDouble() / iterations
                        results = """
                            ✅ 邏輯測試完成！
                            模擬場次：$iterations
                            失敗場次：$totalErrors
                            總計耗時：$duration ms
                            平均每場耗時：$avg ms
                        """.trimIndent()
                        isRunning = false
                    }
                },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRunning && results == null) Text("測試中... ${(progress * 100).toInt()}%")
                else Text("開始 10,000 次邏輯壓力測試")
            }
            
            results?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("API & 資料庫穩定性測試", style = MaterialTheme.typography.titleLarge)
            Text("對 Supabase 發送高併發請求，測試伺服器承受能力。", style = MaterialTheme.typography.bodySmall)
            
            Spacer(Modifier.height(16.dp))

            GCButton(
                onClick = {
                    scope.launch {
                        isRunning = true
                        apiResults = null
                        val concurrentRequests = 50 // 併發數量
                        val startTime = Clock.System.now().toEpochMilliseconds()
                        var successCount = 0
                        var failCount = 0
                        
                        val jobs = List(concurrentRequests) {
                            scope.async {
                                try {
                                    repository.getAllTeams()
                                    true
                                } catch (e: Exception) {
                                    false
                                }
                            }
                        }
                        jobs.forEach { 
                            try {
                                if (it.await()) successCount++ else failCount++
                            } catch (e: Exception) {
                                failCount++
                            }
                        }
                        
                        val endTime = Clock.System.now().toEpochMilliseconds()
                        val duration = endTime - startTime
                        apiResults = """
                            📡 API 併發測試完成！
                            總請求數：$concurrentRequests (同時發送)
                            成功次數：$successCount
                            失敗次數：$failCount
                            總計耗時：$duration ms
                            平均回應時間：${duration.toDouble() / concurrentRequests} ms
                        """.trimIndent()
                        isRunning = false
                    }
                },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (isRunning && apiResults == null) Text("正在與伺服器通訊...")
                else Text("發送 50 個併發請求測試")
            }

            apiResults?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun runMonteCarloIteration() {
    val playerCount = kotlin.random.Random.nextInt(4, 40)
    val totalPar = 72
    
    val participants = mutableListOf<MemberWithUser>()
    val scores = mutableListOf<Score>()

    for (id in 0 until playerCount) {
        val mId = "m_$id"
        participants.add(MemberWithUser(
            id = mId, teamId = "t1", handicap = kotlin.random.Random.nextDouble(0.0, 36.0),
            role = "member", users = User(id = "u_$id", lineUid = "l_$id", realName = "P$id")
        ))
        val holeScores = List(18) { kotlin.random.Random.nextInt(1, 10) }
        scores.add(Score(eventId = "e1", teamMemberId = mId, grossScore = holeScores.sum(), holeScores = holeScores))
    }

    // 測試各種規則
    HandicapCalculator.settleTeamHandicap(scores, participants, totalPar)
    HandicapCalculator.settleNewPeoria(scores, participants, (1..18).shuffled().take(6), totalPar)
}

package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventLeaderboardScreen(eventId: String, isFromScoring: Boolean = false, onBack: () -> Unit) {
    val repository = remember { TeamRepository() }
    var entries by remember { mutableStateOf<List<EventLeaderboardEntry>>(emptyList()) }
    var settledEntries by remember { mutableStateOf<List<HandicapCalculator.SettledScore>>(emptyList()) }
    var eventPars by remember { mutableStateOf(List(18) { 4 }) }
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSettling by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("member") }
    var displayMode by remember { mutableStateOf("summary") } // "summary" or "holes"
    
    // 對抗賽專屬
    var matchResults by remember { mutableStateOf<List<HandicapCalculator.MatchPlayPairingResult>>(emptyList()) }
    var totalPointsA by remember { mutableStateOf(0.0) }
    var totalPointsB by remember { mutableStateOf(0.0) }

    val scope = rememberCoroutineScope()
    
    val currentUser by AuthManager.currentUser.collectAsState()
    val sharedScrollState = rememberScrollState()

    fun loadData() {
        scope.launch {
            try {
                val ev = repository.getEventById(eventId)
                event = ev
                eventPars = repository.getEventPars(eventId)
                
                currentUser?.id?.let { uid ->
                    userRole = repository.getMember(uid, ev?.teamId ?: "")?.role ?: "member"
                }

                if (ev?.handicapRule == "Match_Play") {
                    val results = repository.getMatchPlayResults(eventId)
                    matchResults = results
                    totalPointsA = results.sumOf { it.totalPointsA }
                    totalPointsB = results.sumOf { it.totalPointsB }
                }
                
                if (ev?.registrationStatus == "closed") {
                    // 已結束，顯示結算排名
                    val participants = repository.getEventParticipants(eventId)
                    val scores = repository.getScoresByEvent(eventId)
                    
                    settledEntries = if (ev.handicapRule == "New_New_Peoria") {
                        HandicapCalculator.settleNewPeoria(
                            scores, participants, ev.hiddenHoles ?: emptyList(), eventPars.sum()
                        )
                    } else {
                        HandicapCalculator.settleTeamHandicap(scores, participants, eventPars.sum())
                    }
                } else {
                    // 進行中，顯示即時排名
                    entries = repository.getEventLeaderboard(eventId)
                }
            } catch (e: Exception) {
                println("EventLeaderboardScreen load error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(eventId) {
        while (true) {
            loadData()
            delay(8 * 60 * 1000)
        }
    }

    val isClosed = event?.registrationStatus == "closed"
    val isAllowedToSettle = MemberRoles.isAdmin(userRole) || currentUser?.isSuperAdmin == true

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { 
                    Text(
                        if (isClosed) "賽事最終成績" else "賽事即時排行榜", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) 
                    ) 
                },
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
                        val backText = if (isFromScoring) "回到成績輸入" else "Back"
                        Text(backText, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                actions = {
                    GCButton(
                        onClick = { loadData() },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF4169E1).copy(alpha = 0.5f),
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Refresh", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            )
        },
        bottomBar = {
            if (isClosed && isAllowedToSettle && settledEntries.isNotEmpty()) {
                BottomAppBar {
                    GCButton(
                        onClick = {
                            scope.launch {
                                isSettling = true
                                try {
                                    val updates = settledEntries.associate { it.memberId to it.newTeamHandicap }
                                    repository.batchUpdateMemberHandicaps(eventId, updates)
                                    loadData() // 重新載入以更新按鈕狀態
                                } catch (e: Exception) {
                                } finally {
                                    isSettling = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        enabled = !isSettling && event?.isSettled == false && event?.handicapRule == "Team_Handicap" 
                    ) {
                        if (isSettling) CircularProgressIndicator(Modifier.size(20.dp))
                        else if (event?.isSettled == true) Text("賽事已完成結算")
                        else Text("確認結算並更新球員差點")
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                // 對抗賽總分板
                if (event?.handicapRule == "Match_Play") {
                    MatchPlayLeaderboardView(totalPointsA, totalPointsB, matchResults, eventPars)
                } else {
                    if (isClosed) {
                        // 顯示模式切換按鈕
                        TabRow(
                            selectedTabIndex = if (displayMode == "summary") 0 else 1,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Tab(
                                selected = displayMode == "summary",
                                onClick = { displayMode = "summary" },
                                text = { Text("成績摘要", style = MaterialTheme.typography.labelLarge) }
                            )
                            Tab(
                                selected = displayMode == "holes",
                                onClick = { displayMode = "holes" },
                                text = { Text("18洞詳情", style = MaterialTheme.typography.labelLarge) }
                            )
                        }
                    }

                    if (isClosed && event?.handicapRule == "New_New_Peoria") {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⛳", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "新新貝利亞隱藏洞為：${event?.hiddenHoles?.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Table Header
                    val headerStyle = MaterialTheme.typography.labelMedium.copy(
                        fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    val headerSmallStyle = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.2f,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Row(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("排名", Modifier.width(30.dp), style = headerStyle)
                        Text("姓名", Modifier.width(80.dp), style = headerStyle)
                        
                        if (!isClosed || displayMode == "holes") {
                            Row(modifier = Modifier.weight(1f).horizontalScroll(sharedScrollState)) {
                                for (i in 0 until 18) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                                        Text("${i + 1}", style = headerSmallStyle)
                                        Text("(${eventPars[i]})", style = headerSmallStyle.copy(fontWeight = FontWeight.Normal), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    }
                                }
                                Spacer(Modifier.width(40.dp))
                            }
                        } else {
                            Text("總桿", Modifier.width(45.dp), style = headerStyle, textAlign = TextAlign.Center)
                            Text("差點", Modifier.width(50.dp), style = headerStyle, textAlign = TextAlign.Center)
                            Text("淨桿", Modifier.weight(1f), style = headerStyle, textAlign = TextAlign.Center)
                            if (event?.handicapRule == "Team_Handicap") {
                                Text("新差點", Modifier.width(60.dp), style = headerStyle, textAlign = TextAlign.Center)
                            }
                        }
                        
                        if (!isClosed || displayMode == "holes") {
                            Text("總桿", Modifier.width(45.dp), style = headerStyle, textAlign = TextAlign.Center)
                            Text("+/-", Modifier.width(45.dp), style = headerStyle, textAlign = TextAlign.Center)
                        }
                    }

                    LazyColumn(Modifier.fillMaxSize()) {
                        if (!isClosed) {
                            itemsIndexed(entries) { index, entry ->
                                LeaderboardRow(index + 1, entry, eventPars, sharedScrollState)
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        } else {
                            items(settledEntries) { entry ->
                                if (displayMode == "holes") {
                                    // 詳情模式：複用即時排行榜的 Row
                                    // 將 SettledScore 轉換為 EventLeaderboardEntry 的格式以供複用
                                    val mappedEntry = EventLeaderboardEntry(
                                        memberId = entry.memberId,
                                        realName = entry.realName,
                                        holeScores = entry.holeScores,
                                        grossScore = entry.grossScore,
                                        toPar = entry.toPar,
                                        holesPlayed = 18,
                                        isFinished = true
                                    )
                                    LeaderboardRow(entry.rank, mappedEntry, eventPars, sharedScrollState)
                                } else {
                                    FinalResultRow(entry, event?.handicapRule ?: "")
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchPlayLeaderboardView(
    totalA: Double, 
    totalB: Double, 
    results: List<HandicapCalculator.MatchPlayPairingResult>,
    eventPars: List<Int>
) {
    var displayMode by remember { mutableStateOf("points") } // "points" or "strokes"
    var selectedSubTab by remember { mutableStateOf(0) } // 0: 對戰組合, 1: 組別排名
    val scrollState = rememberScrollState()

    val scale = 1.2f
    val baseSmall = MaterialTheme.typography.bodySmall
    val scaledSmall = baseSmall.copy(fontSize = baseSmall.fontSize * scale)
    val scaledTitle = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * scale)
    val scaledLabel = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * scale)

    Column(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                // Label A on Left
                Text(
                    "A 隊總分", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                
                // Label B on Right
                Text(
                    "B 隊總分", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "$totalA", 
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), 
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Box(Modifier.padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                        Text("VS", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    }
                    
                    Text(
                        "$totalB", 
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), 
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // 第一層：切換 積分/桿數
        TabRow(
            selectedTabIndex = if (displayMode == "points") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            Tab(
                selected = displayMode == "points", 
                onClick = { displayMode = "points" }, 
                text = { Text("積分模式 (Points)", style = MaterialTheme.typography.labelLarge) }
            )
            Tab(
                selected = displayMode == "strokes", 
                onClick = { displayMode = "strokes" }, 
                text = { Text("桿數模式 (Strokes)", style = MaterialTheme.typography.labelLarge) }
            )
        }

        // 第二層：使用更簡約的樣式，改為橘色
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orange = androidx.compose.ui.graphics.Color(0xFFFF9800)
            GCFilterChip(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                label = { Text("對戰詳情") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = orange,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
            )
            GCFilterChip(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                label = { Text("組別排名") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = orange,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            if (selectedSubTab == 0) {
                // --- 對戰組合視圖 ---
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("對戰組合明細", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * scale), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    // 統一表頭樣式 (深色背景)
                    val headerTextStyle = scaledLabel.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    
                    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("組別 / Hole", modifier = Modifier.width(100.dp), style = headerTextStyle)
                            Row(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                                (1..18).forEach { h ->
                                    Text("$h", modifier = Modifier.width(35.dp), textAlign = TextAlign.Center, style = headerTextStyle)
                                }
                                Text("Total", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = headerTextStyle)
                            }
                        }
                        if (displayMode == "strokes") {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Par", modifier = Modifier.width(100.dp), style = headerTextStyle.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)))
                                Row(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                                    eventPars.forEach { p ->
                                        Text("$p", modifier = Modifier.width(35.dp), textAlign = TextAlign.Center, style = headerTextStyle.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)))
                                    }
                                    Text("${eventPars.sum()}", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = headerTextStyle.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)))
                                }
                            }
                        }
                    }
                }

                items(results) { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            // Side A Row
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.width(100.dp)) {
                                    Text("A-${res.groupANumber}", style = scaledTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(res.membersA.joinToString("/"), style = scaledLabel, maxLines = 1, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                                    if (displayMode == "points") {
                                        res.holeResults.forEach { h ->
                                            Text(
                                                if (h.pointsA == 0.0) "-" else h.pointsA.toString(),
                                                modifier = Modifier.width(35.dp),
                                                textAlign = TextAlign.Center,
                                                style = scaledSmall,
                                                fontWeight = if (h.winnerSide == "A") FontWeight.Bold else FontWeight.Normal,
                                                color = if (h.winnerSide == "A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text("${res.totalPointsA}", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = scaledTitle.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        res.bestScoresA.forEachIndexed { i, s ->
                                            Box(modifier = Modifier.width(35.dp), contentAlignment = Alignment.Center) {
                                                ScoreBadge(s, eventPars[i])
                                            }
                                        }
                                        Text("${res.bestScoresA.sum()}", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = scaledTitle.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                            // Side B Row
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.width(100.dp)) {
                                    Text("B-${res.groupBNumber}", style = scaledTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(res.membersB.joinToString("/"), style = scaledLabel, maxLines = 1, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                                    if (displayMode == "points") {
                                        res.holeResults.forEach { h ->
                                            Text(
                                                if (h.pointsB == 0.0) "-" else h.pointsB.toString(),
                                                modifier = Modifier.width(35.dp),
                                                textAlign = TextAlign.Center,
                                                style = scaledSmall,
                                                fontWeight = if (h.winnerSide == "B") FontWeight.Bold else FontWeight.Normal,
                                                color = if (h.winnerSide == "B") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text("${res.totalPointsB}", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = scaledTitle.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                                    } else {
                                        res.bestScoresB.forEachIndexed { i, s ->
                                            Box(modifier = Modifier.width(35.dp), contentAlignment = Alignment.Center) {
                                                ScoreBadge(s, eventPars[i])
                                            }
                                        }
                                        Text("${res.bestScoresB.sum()}", modifier = Modifier.width(45.dp), textAlign = TextAlign.Center, style = scaledTitle.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                            
                            if (displayMode == "points") {
                                val allBonuses = res.holeResults.flatMap { it.individualBonuses }
                                if (allBonuses.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("☀️ 成就加分: " + allBonuses.groupBy { it.first }.map { "${it.key}(+${it.value.sumOf { p -> p.second }})" }.joinToString(", "), style = scaledLabel, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // --- 組別排名視圖 (Flattened Ranking) ---
                val flattenedGroups = results.flatMap { res ->
                    listOf(
                        GroupRankInfo("A", res.groupANumber, res.membersA, res.totalPointsA, res.bestScoresA),
                        GroupRankInfo("B", res.groupBNumber, res.membersB, res.totalPointsB, res.bestScoresB)
                    )
                }

                val sortedGroups = if (displayMode == "points") {
                    flattenedGroups.sortedByDescending { it.points }
                } else {
                    flattenedGroups.sortedBy { it.scores.sum() }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text("全體組別排名", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * scale), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                }

                itemsIndexed(sortedGroups) { index, info ->
                    val rankColor = when (index) {
                        0 -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                        1 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
                        2 -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
                        else -> MaterialTheme.colorScheme.outline
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "${index + 1}", 
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), 
                                    color = rankColor
                                )
                            }
                            
                            Column(Modifier.weight(1f)) {
                                Text("${info.side}-${info.number} 組", style = scaledLabel, fontWeight = FontWeight.Bold, color = if (info.side == "A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                Text(
                                    info.members.joinToString(" / "), 
                                    style = scaledTitle.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val valueStr = if (displayMode == "points") "${info.points} pt" else "${info.scores.sum()} 桿"
                                Text(
                                    valueStr, 
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class GroupRankInfo(
    val side: String,
    val number: Int,
    val members: List<String>,
    val points: Double,
    val scores: List<Int>
)

@Composable
fun FinalResultRow(entry: HandicapCalculator.SettledScore, rule: String) {
    val nameStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f,
        fontWeight = FontWeight.Bold
    )

    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${entry.rank}", Modifier.width(30.dp), style = MaterialTheme.typography.bodyMedium)
        Text(entry.realName, Modifier.width(80.dp), style = nameStyle)
        Text("${entry.grossScore}", Modifier.width(45.dp), style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f), textAlign = TextAlign.Center)
        
        val displayHcp = if (rule == "New_New_Peoria") entry.appliedHandicap else entry.currentTeamHandicap
        val hcpStr = ((displayHcp * 10).toInt() / 10.0).toString()
        Text(hcpStr, Modifier.width(50.dp), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
        
        val netStr = ((entry.netScore * 10).toInt() / 10.0).toString()
        Text(
            netStr, 
            Modifier.weight(1f), 
            style = MaterialTheme.typography.headlineSmall, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (rule == "Team_Handicap") {
            val diff = entry.newTeamHandicap - entry.currentTeamHandicap
            val diffStr = if (diff < 0) " (${diff.toInt()})" else ""
            Text(
                "${entry.newTeamHandicap}$diffStr",
                Modifier.width(60.dp), 
                style = MaterialTheme.typography.bodySmall, 
                textAlign = TextAlign.Center,
                color = if (diff < 0) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, entry: EventLeaderboardEntry, pars: List<Int>, scrollState: androidx.compose.foundation.ScrollState) {
    val nameStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f,
        fontWeight = FontWeight.Bold
    )

    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$rank", Modifier.width(30.dp), style = MaterialTheme.typography.bodyMedium)
        Text(entry.realName, Modifier.width(80.dp), style = nameStyle)
        
        // 使用 Row 搭配 horizontalScroll 實作連動內容
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(scrollState)
        ) {
            for (i in 0 until 18) {
                val score = entry.holeScores.getOrNull(i) ?: 0
                val par = pars.getOrNull(i) ?: 4
                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(32.dp)) {
                    ScoreBadge(score, par)
                }
            }
            Spacer(Modifier.width(40.dp))
        }
        
        Text(
            text = "${entry.grossScore}", 
            modifier = Modifier.width(45.dp), 
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f), 
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        // 對抗賽顯示積分，一般賽制顯示 +/-
        if (entry.points > 0 || entry.grossScore > 0) { 
             val displayText = if (entry.points > 0) "${entry.points}pt" 
                               else if (entry.toPar > 0) "+${entry.toPar}" 
                               else if (entry.toPar < 0) "${entry.toPar}" 
                               else "E"
             
             Text(
                text = displayText, 
                modifier = Modifier.width(45.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.2f),
                color = if (entry.points > 0) MaterialTheme.colorScheme.secondary 
                        else if (entry.toPar > 0) MaterialTheme.colorScheme.error 
                        else if (entry.toPar < 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun ScoringScreen(eventId: String, groupId: String, onNavigateToLeaderboard: () -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    var currentHole by remember { mutableStateOf(1) }
    var playerScores by remember { mutableStateOf(mutableMapOf<String, MutableList<Int>>()) }
    var players by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }
    var eventPars by remember { mutableStateOf(List(18) { 4 }) }
    var isLoading by remember { mutableStateOf(true) }
    var scoringError by remember { mutableStateOf<String?>(null) }
    var isEventClosed by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("member") }
    
    // 對抗賽專屬狀態
    var eventObj by remember { mutableStateOf<Event?>(null) }
    var opponentPlayers by remember { mutableStateOf<List<MemberWithUser>>(emptyList()) }

    val currentUser by AuthManager.currentUser.collectAsState()
    val canEdit = !isEventClosed || MemberRoles.isAdmin(userRole) || currentUser?.isSuperAdmin == true

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(eventId, groupId, currentUser) {
        isLoading = true
        try {
            val event = repository.getEventById(eventId)
            eventObj = event
            isEventClosed = event?.registrationStatus == "closed"
            
            currentUser?.id?.let { uid ->
                userRole = repository.getMember(uid, event?.teamId ?: "")?.role ?: "member"
            }

            val members = repository.getGroupMembers(groupId)
            players = members
            eventPars = repository.getEventPars(eventId)
            
            // 讀取自己這組的成績
            val scoresMap = mutableMapOf<String, MutableList<Int>>()
            members.forEach { m -> scoresMap[m.id] = MutableList(18) { 0 } }
            
            val allScores = repository.getScoresByEvent(eventId)
            
            // 處理自己的成績
            allScores.forEach { s -> 
                if (scoresMap.containsKey(s.teamMemberId)) { 
                    scoresMap[s.teamMemberId] = s.holeScores.toMutableList() 
                } 
            }

            // 對抗賽邏輯：抓取對手組別資訊並整合進計分 Map
            if (event?.handicapRule == "Match_Play") {
                val pairings = repository.getMatchPairings(eventId)
                val pairing = pairings.find { it.groupAId == groupId || it.groupBId == groupId }
                if (pairing != null) {
                    val oppId = if (pairing.groupAId == groupId) pairing.groupBId else pairing.groupAId
                    val oppPlayers = repository.getGroupMembers(oppId)
                    opponentPlayers = oppPlayers
                    
                    // 將對手也加入可編輯的 Map 中
                    oppPlayers.forEach { m -> 
                        val existingScore = allScores.find { it.teamMemberId == m.id }
                        scoresMap[m.id] = existingScore?.holeScores?.toMutableList() ?: MutableList(18) { 0 }
                    }
                }
            }
            playerScores = scoresMap

            // 自動跳轉到第一個還沒填成績的洞
            val isMatchPlay = event?.handicapRule == "Match_Play"
            val firstEmptyHole = (1..18).firstOrNull { h ->
                if (isMatchPlay) {
                    // 對抗賽：只要這組有人(通常是代表)填過，就視為已完成該洞
                    players.all { p -> (playerScores[p.id]?.get(h - 1) ?: 0) == 0 }
                } else {
                    // 一般賽制：任何人沒填都視為未完成
                    players.any { p -> (playerScores[p.id]?.get(h - 1) ?: 0) == 0 }
                }
            } ?: 1
            currentHole = firstEmptyHole

        } catch (e: Exception) {
            println("ScoringScreen load error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GCButton(
                            onClick = onBack,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("< Back", style = MaterialTheme.typography.titleMedium)
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        val par = eventPars.getOrNull(currentHole - 1) ?: 4
                        Text("第 $currentHole 洞 (Par $par)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        GCButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    repository.submitScores(eventId, playerScores.mapValues { it.value.toList() })
                                }
                            },
                            enabled = canEdit,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Save", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    )
 { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    GCButton(onClick = { if (currentHole > 1) currentHole-- else currentHole = 18 }) { Text("Prev") }
                    
                    GCButton(
                        onClick = onNavigateToLeaderboard,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFFF9800),
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Black),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
                    ) {
                        Text(
                            "賽事排行", 
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    GCButton(
                        onClick = { 
                            scope.launch {
                                repository.submitScores(eventId, playerScores.mapValues { it.value.toList() })
                                if (currentHole < 18) currentHole++ else currentHole = 1
                            }
                        }
                    ) { Text("Next") }
                }
                
                // 對抗賽積分預覽
                if (eventObj?.handicapRule == "Match_Play") {
                    val par = eventPars.getOrNull(currentHole - 1) ?: 4
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("比洞賽 - 本洞積分預覽", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            
                            // 計算本組在該洞的表現
                            val groupScores = players.map { playerScores[it.id]?.getOrNull(currentHole - 1) ?: 0 }
                            val bestScore = groupScores.filter { it > 0 }.minOrNull()
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("本組最佳：${bestScore ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                                
                                // 顯示對手最佳桿數
                                val oppGroupScores = opponentPlayers.map { playerScores[it.id]?.getOrNull(currentHole - 1) ?: 0 }
                                val oppBestScore = oppGroupScores.filter { it > 0 }.minOrNull()
                                Spacer(Modifier.width(16.dp))
                                Text("對手最佳：${oppBestScore ?: "-"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 成就加分預覽
                                val bonus = players.sumOf { p ->
                                    val s = playerScores[p.id]?.getOrNull(currentHole - 1) ?: 0
                                    when {
                                        s <= 0 -> 0.0
                                        s == 1 -> 1.0
                                        s <= par - 2 -> 1.0
                                        s == par - 1 -> 0.5
                                        else -> 0.0
                                    }
                                }
                                if (bonus > 0) {
                                    Text("⛳ 成就加分：+$bonus", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Text("註：完整勝負分需對手同步上傳成績後計算", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    // --- 區塊一：本組球員 ---
                    item {
                        val sideText = players.firstOrNull()?.groupSide
                        val label = if (eventObj?.handicapRule == "Match_Play" && sideText != null) {
                            "本組 (Side $sideText)"
                        } else {
                            "本組球員"
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    itemsIndexed(players) { index, player ->
                        val memberId = player.id
                        val scores = playerScores[memberId] ?: return@itemsIndexed
                        val currentScore = scores[currentHole - 1]
                        val totalScore = scores.sum()
                        
                        // 對抗賽規則：只有第一個人能輸入
                        val isLocked = eventObj?.handicapRule == "Match_Play" && index > 0
                        val rowCanEdit = canEdit && !isLocked

                        PlayerScoreCard(
                            player = player,
                            currentScore = currentScore,
                            totalScore = totalScore,
                            canEdit = rowCanEdit,
                            isLocked = isLocked,
                            onScoreChange = { delta ->
                                val newScores = playerScores.toMutableMap()
                                val holeList = newScores[memberId]!!.toMutableList()
                                val newValue = holeList[currentHole - 1] + delta
                                if (newValue >= 1) {
                                    holeList[currentHole - 1] = newValue
                                    newScores[memberId] = holeList
                                    playerScores = newScores
                                }
                            }
                        )
                    }

                    // --- 區塊二：對手球員 (僅對抗賽顯示) ---
                    if (eventObj?.handicapRule == "Match_Play" && opponentPlayers.isNotEmpty()) {
                        item {
                            val oppSideText = opponentPlayers.firstOrNull()?.groupSide
                            val oppLabel = if (oppSideText != null) "對手 (Side $oppSideText)" else "對手球員"
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = oppLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        itemsIndexed(opponentPlayers) { index, player ->
                            val memberId = player.id
                            val scores = playerScores[memberId] ?: return@itemsIndexed
                            val currentScore = scores[currentHole - 1]
                            val totalScore = scores.sum()
                            
                            val isLocked = index > 0
                            val rowCanEdit = canEdit && !isLocked

                            PlayerScoreCard(
                                player = player,
                                currentScore = currentScore,
                                totalScore = totalScore,
                                canEdit = rowCanEdit,
                                isLocked = isLocked,
                                onScoreChange = { delta ->
                                    val newScores = playerScores.toMutableMap()
                                    val holeList = newScores[memberId]!!.toMutableList()
                                    val newValue = holeList[currentHole - 1] + delta
                                    if (newValue >= 1) {
                                        holeList[currentHole - 1] = newValue
                                        newScores[memberId] = holeList
                                        playerScores = newScores
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScoreCard(
    player: MemberWithUser,
    currentScore: Int,
    totalScore: Int,
    canEdit: Boolean,
    isLocked: Boolean,
    onScoreChange: (Int) -> Unit
) {
    val nameStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.5f,
        fontWeight = FontWeight.Bold
    )
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.users.realName, style = nameStyle)
                if (isLocked) {
                    Text("由隊友代表輸入", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    Text("Total: $totalScore", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 減號按鈕
                GCElevatedCard(
                    onClick = { if (canEdit) onScoreChange(-1) },
                    modifier = Modifier.size(48.dp),
                    enabled = canEdit,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("-", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                // 分數顯示
                Surface(
                    modifier = Modifier.size(width = 60.dp, height = 48.dp),
                    shape = MaterialTheme.shapes.small,
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.DarkGray),
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentScore == 0) "-" else currentScore.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (canEdit) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // 加號按鈕
                GCElevatedCard(
                    onClick = { if (canEdit) onScoreChange(1) },
                    modifier = Modifier.size(48.dp),
                    enabled = canEdit,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}

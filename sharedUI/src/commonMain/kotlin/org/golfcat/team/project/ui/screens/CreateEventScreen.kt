package org.golfcat.team.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import org.golfcat.team.project.*
import org.golfcat.team.project.models.*
import org.golfcat.team.project.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(teamId: String, editEventId: String? = null, duplicateFromId: String? = null, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { TeamRepository() }
    
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    
    val timePickerState = rememberTimePickerState(is24Hour = true)
    var showTimePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("") }
    
    var groupCount by remember { mutableStateOf("") }
    
    var location by remember { mutableStateOf("") }
    var suggestedCourses by remember { mutableStateOf<List<CourseMaster>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }

    var handicapRule by remember { mutableStateOf("Team_Handicap") }
    var matchPlaySubMode by remember { mutableStateOf("Four_Ball") }
    var pars by remember { mutableStateOf(List(18) { "4" }) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 追蹤原始賽制，判斷是否切換到新新貝利亞
    var originalHandicapRule by remember { mutableStateOf("") }
    var showPeoriaStrategyDialog by remember { mutableStateOf(false) }

    // 如果是編輯模式或複製模式，載入初始資料
    LaunchedEffect(editEventId, duplicateFromId) {
        val targetId = editEventId ?: duplicateFromId
        if (!targetId.isNullOrBlank()) {
            isLoading = true
            try {
                val ev = repository.getEventById(targetId)
                if (ev != null) {
                    location = ev.location
                    if (editEventId != null) {
                        date = ev.date
                        startTime = ev.startTime ?: ""
                    }
                    groupCount = ev.groupCount?.toString() ?: ""
                    handicapRule = ev.handicapRule
                    originalHandicapRule = ev.handicapRule
                    matchPlaySubMode = ev.matchPlaySubMode ?: "Four_Ball"
                    pars = ev.pars.map { it.toString() }
                }
            } catch (e: Exception) {
                errorMessage = "載入賽事資料失敗"
            } finally {
                isLoading = false
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                GCTextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val dateObj = instant.toLocalDateTime(TimeZone.UTC).date
                        date = dateObj.toString()
                    }
                    showDatePicker = false
                }) { Text("確定") }
            },
            dismissButton = {
                GCTextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                GCTextButton(onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    startTime = "$hour:$minute"
                    showTimePicker = false
                }) { Text("確定") }
            },
            dismissButton = {
                GCTextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { Text(if (!editEventId.isNullOrBlank()) "變更賽事資訊" else "建立新球賽", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
                }
            )
        }
    ) { padding ->
        val cardModifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        val cardColors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
        val cardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        val cardBorder = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            item {
                // 卡片一：基本資訊
                Card(modifier = cardModifier, colors = cardColors, elevation = cardElevation, border = cardBorder) {
                    Column(Modifier.padding(16.dp)) {
                        Text("基本資訊", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = date,
                            onValueChange = { },
                            label = { Text("比賽日期") },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { },
                                label = { Text("開球時間") },
                                modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                            OutlinedTextField(
                                value = groupCount,
                                onValueChange = { if (it.all { c -> c.isDigit() }) groupCount = it },
                                label = { Text("組數") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { 
                                    location = it
                                    scope.launch {
                                        suggestedCourses = repository.searchCourses(it)
                                        showSuggestions = suggestedCourses.isNotEmpty()
                                    }
                                },
                                label = { Text("地點 (球場名稱)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            DropdownMenu(
                                expanded = showSuggestions,
                                onDismissRequest = { showSuggestions = false }
                            ) {
                                suggestedCourses.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.name) },
                                        onClick = {
                                            location = course.name
                                            pars = course.pars.map { it.toString() }
                                            showSuggestions = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 卡片二：標準桿
                Card(modifier = cardModifier, colors = cardColors, elevation = cardElevation, border = cardBorder) {
                    Column(Modifier.padding(16.dp)) {
                        val totalPar = pars.sumOf { it.toIntOrNull() ?: 0 }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("標準桿設定", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(
                                "總標準桿: $totalPar", 
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize * 1.5f,
                                    fontWeight = FontWeight.Bold
                                ), 
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        val holeValueStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.2f,
                            fontWeight = FontWeight.Bold
                        )

                        // 18洞輸入區 (分為兩排，每排9洞)
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            listOf(0..8, 9..17).forEach { range ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    range.forEach { holeIndex ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "${holeIndex + 1}", 
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 3f
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            // 加號按鈕
                                            GCElevatedCard(
                                                onClick = {
                                                    val currentVal = pars[holeIndex].toIntOrNull() ?: 4
                                                    if (currentVal < 6) {
                                                        val newList = pars.toMutableList()
                                                        newList[holeIndex] = (currentVal + 1).toString()
                                                        pars = newList
                                                    }
                                                },
                                                modifier = Modifier.size(44.dp),
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                            ) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            val currentPar = pars[holeIndex].toIntOrNull() ?: 4
                                            val parTextColor = when(currentPar) {
                                                3 -> androidx.compose.ui.graphics.Color(0xFF1B5E20) // 深綠
                                                4 -> androidx.compose.ui.graphics.Color(0xFF1A237E) // 深藍
                                                else -> MaterialTheme.colorScheme.error // 紅 (5, 6)
                                            }

                                            Surface(
                                                modifier = Modifier.size(width = 44.dp, height = 44.dp),
                                                shape = MaterialTheme.shapes.small,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.DarkGray),
                                                color = androidx.compose.ui.graphics.Color.White,
                                                shadowElevation = 2.dp
                                            ) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = pars[holeIndex], 
                                                        style = holeValueStyle,
                                                        color = parTextColor
                                                    )
                                                }
                                            }
                                            
                                            // 減號按鈕
                                            GCElevatedCard(
                                                onClick = {
                                                    val currentVal = pars[holeIndex].toIntOrNull() ?: 4
                                                    if (currentVal > 3) {
                                                        val newList = pars.toMutableList()
                                                        newList[holeIndex] = (currentVal - 1).toString()
                                                        pars = newList
                                                    }
                                                },
                                                modifier = Modifier.size(44.dp),
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                            ) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 卡片三：賽制設定
                Card(modifier = cardModifier, colors = cardColors, elevation = cardElevation, border = cardBorder) {
                    Column(Modifier.padding(16.dp)) {
                        Text("賽制設定", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        val ruleTextStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                GCRadioButton(selected = handicapRule == "Team_Handicap", onClick = { handicapRule = "Team_Handicap" })
                                Text("球隊差點", style = ruleTextStyle)
                                Spacer(Modifier.width(12.dp))
                                GCRadioButton(selected = handicapRule == "New_New_Peoria", onClick = { handicapRule = "New_New_Peoria" })
                                Text("新新貝利亞", style = ruleTextStyle)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                GCRadioButton(selected = handicapRule == "Match_Play", onClick = { handicapRule = "Match_Play" })
                                Text("分組對抗", style = ruleTextStyle)
                            }
                        }
                        
                        if (handicapRule == "Match_Play") {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("對抗賽模式", style = ruleTextStyle, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GCRadioButton(selected = matchPlaySubMode == "Four_Ball", onClick = { matchPlaySubMode = "Four_Ball" })
                                        Text("比洞賽-四人四球最佳球位", style = ruleTextStyle)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GCRadioButton(selected = matchPlaySubMode == "Foursomes", onClick = { matchPlaySubMode = "Foursomes" })
                                        Text("比洞賽-四人二球", style = ruleTextStyle)
                                    }
                                }
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))
                GCButton(
                    onClick = {
                        if (!editEventId.isNullOrBlank() && handicapRule == "New_New_Peoria" && originalHandicapRule != "New_New_Peoria") {
                            // 只有在「編輯模式」且「賽制改為新新貝利亞」時，才詢問策略
                            showPeoriaStrategyDialog = true
                        } else {
                            scope.launch {
                                isLoading = true
                                try {
                                    if (!editEventId.isNullOrBlank()) {
                                        repository.updateEvent(
                                            eventId = editEventId,
                                            location = location,
                                            date = date,
                                            handicapRule = handicapRule,
                                            pars = pars.map { it.toIntOrNull() ?: 4 },
                                            startTime = startTime,
                                            groupCount = groupCount.toIntOrNull(),
                                            matchPlaySubMode = if (handicapRule == "Match_Play") matchPlaySubMode else null
                                        )
                                    } else {
                                        repository.createEvent(
                                            teamId = teamId,
                                            location = location,
                                            date = date,
                                            handicapRule = handicapRule,
                                            pars = pars.map { it.toIntOrNull() ?: 4 },
                                            startTime = startTime,
                                            groupCount = groupCount.toIntOrNull(),
                                            matchPlaySubMode = if (handicapRule == "Match_Play") matchPlaySubMode else null
                                        )
                                    }
                                    
                                    // 自動貢獻資料到球場大師庫
                                    repository.saveCourseMaster(
                                        CourseMaster(
                                            name = location,
                                            pars = pars.map { it.toIntOrNull() ?: 4 },
                                            createdByTeamId = teamId
                                        )
                                    )

                                    onBack()
                                } catch (e: Exception) {
                                    errorMessage = "${if (!editEventId.isNullOrBlank()) "更新" else "建立"}失敗: ${e.message}"
                                    println("操作賽事失敗: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && location.isNotBlank() && date.isNotBlank()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text(if (!editEventId.isNullOrBlank()) "儲存變更" else "建立球賽")
                }
            }
        }
    }

    if (showPeoriaStrategyDialog && !editEventId.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showPeoriaStrategyDialog = false },
            title = { Text("賽制轉換策略") },
            text = { Text("您已將賽制改為「新新貝利亞」，請問隱藏洞要如何決定？") },
            confirmButton = {
                GCButton(onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            repository.updateEvent(
                                eventId = editEventId,
                                location = location,
                                date = date,
                                handicapRule = handicapRule,
                                pars = pars.map { it.toIntOrNull() ?: 4 },
                                startTime = startTime,
                                groupCount = groupCount.toIntOrNull(),
                                matchPlaySubMode = if (handicapRule == "Match_Play") matchPlaySubMode else null,
                                newPeoriaStrategy = "RANDOM"
                            )
                            showPeoriaStrategyDialog = false
                            onBack()
                        } catch (e: Exception) {
                            errorMessage = "更新失敗: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) { Text("系統隨機挑選") }
            },
            dismissButton = {
                GCTextButton(onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            repository.updateEvent(
                                eventId = editEventId,
                                location = location,
                                date = date,
                                handicapRule = handicapRule,
                                pars = pars.map { it.toIntOrNull() ?: 4 },
                                startTime = startTime,
                                groupCount = groupCount.toIntOrNull(),
                                matchPlaySubMode = if (handicapRule == "Match_Play") matchPlaySubMode else null,
                                newPeoriaStrategy = "VOTE"
                            )
                            showPeoriaStrategyDialog = false
                            onBack()
                        } catch (e: Exception) {
                            errorMessage = "更新失敗: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) { Text("開放球員投票") }
            }
        )
    }
}

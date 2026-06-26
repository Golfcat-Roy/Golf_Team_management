package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.golfcat.team.project.models.HandicapCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    eventId: String,
    memberId: String,
    repository: TeamRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentHoleIndex by remember { mutableStateOf(0) }
    var holeScores by remember { mutableStateOf(List(18) { 0 }) }
    var pars by remember { mutableStateOf(List(18) { 4 }) }
    var isLoading by remember { mutableStateOf(true) }

    // 💡 Load initial data
    LaunchedEffect(eventId, memberId) {
        isLoading = true
        holeScores = repository.getMemberScore(eventId, memberId)
        pars = repository.getEventPars(eventId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ResStrings.SCORING_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 💡 Hole Selection
                ScrollableTabRow(
                    selectedTabIndex = currentHoleIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    for (i in 1..18) {
                        Tab(
                            selected = currentHoleIndex == i - 1,
                            onClick = { currentHoleIndex = i - 1 },
                            text = { Text("H$i") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "${ResStrings.HOLE} ${currentHoleIndex + 1}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ResStrings.PAR} ${pars[currentHoleIndex]}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // 💡 Using HorizontalRule as a Minus icon if Remove is missing
                    FilledIconButton(
                        onClick = {
                            val newVal = if (holeScores[currentHoleIndex] > 1) holeScores[currentHoleIndex] - 1 else 1
                            val newList = holeScores.toMutableList()
                            newList[currentHoleIndex] = newVal
                            holeScores = newList
                            scope.launch { repository.updateHoleScore(eventId, memberId, currentHoleIndex, newVal) }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text("-", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = holeScores[currentHoleIndex].toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    FilledIconButton(
                        onClick = {
                            val newVal = holeScores[currentHoleIndex] + 1
                            val newList = holeScores.toMutableList()
                            newList[currentHoleIndex] = newVal
                            holeScores = newList
                            scope.launch { repository.updateHoleScore(eventId, memberId, currentHoleIndex, newVal) }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(ResStrings.GROSS_SCORE, HandicapCalculator.getGrossScore(holeScores).toString())
                        SummaryItem(ResStrings.NET_SCORE, "--")
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

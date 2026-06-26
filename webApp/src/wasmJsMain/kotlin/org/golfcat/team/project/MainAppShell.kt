package org.golfcat.team.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell() {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        TabData(ResStrings.TAB_EVENTS, Icons.Default.DateRange),
        TabData(ResStrings.TAB_HISTORY, Icons.AutoMirrored.Filled.List),
        TabData(ResStrings.TAB_RANKING, Icons.Default.Star),
        TabData(ResStrings.TAB_TEAM, Icons.Default.Person),
        TabData(ResStrings.TAB_GUIDE, Icons.Default.Info)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ResStrings.APP_NAME) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> EventListTabMock()
                else -> PlaceholderTab(tabs[selectedTab].title)
            }
        }
    }
}

data class TabData(val title: String, val icon: ImageVector)

@Composable
fun EventListTabMock() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(ResStrings.EVENT_LIST_TITLE, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2024 Summer Open", style = MaterialTheme.typography.titleMedium)
                Text("${ResStrings.EVENT_LOCATION}: Daxi Golf Course")
                Text("${ResStrings.EVENT_DATE}: 2024-06-25")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}) {
                    Text(ResStrings.EVENT_JOIN)
                }
            }
        }
    }
}

@Composable
fun PlaceholderTab(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("$title Page (Under Construction)")
    }
}

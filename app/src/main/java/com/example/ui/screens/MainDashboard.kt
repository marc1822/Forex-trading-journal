package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserProfile
import com.example.ui.TradingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var currentTab by remember { mutableStateOf(0) }

    val profile = userProfile ?: UserProfile()
    val traderName = profile.traderName.ifBlank { "Trader" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Forex Journal",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Welcome, $traderName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    NavItem("Journal", Icons.Default.CalendarMonth, "nav_journal"),
                    NavItem("Rules", Icons.Default.Gavel, "nav_rules"),
                    NavItem("Analytics", Icons.Default.Poll, "nav_analytics"),
                    NavItem("AI Coach", Icons.Default.AutoAwesome, "nav_coach"),
                    NavItem("Profile", Icons.Default.Person, "nav_profile")
                )

                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> JournalTab(viewModel = viewModel)
                1 -> RulesTab(viewModel = viewModel)
                2 -> AnalyticsTabContainer(viewModel = viewModel)
                3 -> AiCoachTab(viewModel = viewModel)
                4 -> ProfileTab(viewModel = viewModel)
            }
        }
    }
}

// Inner Tab container for Analytics and Win-Rate Calculator to keep it tidy!
@Composable
fun AnalyticsTabContainer(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            modifier = Modifier.fillMaxWidth().testTag("analytics_sub_tab_row")
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Performance & Growth", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("sub_tab_performance")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Win Calculator", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("sub_tab_calculator")
            )
        }

        when (selectedSubTab) {
            0 -> StatsTab(viewModel = viewModel)
            1 -> CalculatorTab(viewModel = viewModel)
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

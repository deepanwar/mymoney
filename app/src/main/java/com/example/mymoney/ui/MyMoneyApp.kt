package com.example.mymoney.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mymoney.ui.screens.ActivityScreen
import com.example.mymoney.ui.screens.InsightScreen
import com.example.mymoney.ui.screens.OverviewScreen
import com.example.mymoney.ui.screens.ProfileScreen
import com.example.mymoney.ui.screens.SharedScreen
import java.time.YearMonth

enum class Tab(val label: String, val icon: ImageVector) {
    Overview("Overview", Icons.Filled.Dashboard),
    Activity("Activity", Icons.AutoMirrored.Filled.List),
    Shared("Shared", Icons.Filled.People),
    Insight("Insight", Icons.Filled.Insights),
}

@Composable
fun MyMoneyApp() {
    var tab by rememberSaveable { mutableStateOf(Tab.Overview) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }

    BackHandler(enabled = showProfile) { showProfile = false }

    if (showProfile) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = { showProfile = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { padding -> ProfileScreen(Modifier.padding(padding)) }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                selectedMonth = month,
                onMonthSelected = { month = it },
                onProfileClick = { showProfile = true },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        val m = Modifier.padding(padding)
        when (tab) {
            Tab.Overview -> OverviewScreen(m)
            Tab.Activity -> ActivityScreen(m)
            Tab.Shared -> SharedScreen(m)
            Tab.Insight -> InsightScreen(m)
        }
    }
}

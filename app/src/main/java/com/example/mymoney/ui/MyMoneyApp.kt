package com.example.mymoney.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.mymoney.ui.addexpense.AddExpenseFlow
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

/** Full-screen destinations shown on top of the tabbed home. */
enum class Destination(val title: String) {
    Profile("Profile"),
    AddExpense("Add expense"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyMoneyApp() {
    var tab by rememberSaveable { mutableStateOf(Tab.Overview) }
    var destination by rememberSaveable { mutableStateOf<Destination?>(null) }
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = destination != null) { destination = null }
    BackHandler(enabled = destination == null && fabMenuExpanded) { fabMenuExpanded = false }

    if (destination == Destination.AddExpense) {
        // TODO: persist the expense once storage exists.
        AddExpenseFlow(onClose = { destination = null }, onSave = { destination = null })
        return
    }

    destination?.let { dest ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(dest.title) },
                    navigationIcon = {
                        IconButton(onClick = { destination = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { padding ->
            val m = Modifier.padding(padding)
            when (dest) {
                Destination.Profile -> ProfileScreen(m)
                Destination.AddExpense -> Unit // Rendered full-screen above.
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                selectedMonth = month,
                onMonthSelected = { month = it },
                onProfileClick = { destination = Destination.Profile },
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
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = it },
                        modifier = Modifier.semantics {
                            contentDescription = if (fabMenuExpanded) "Close add menu" else "Open add menu"
                        },
                    ) {
                        val icon by remember {
                            derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
                        }
                        Icon(
                            painter = rememberVectorPainter(icon),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                },
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        destination = Destination.AddExpense
                    },
                    icon = { Icon(Icons.Filled.RemoveCircleOutline, contentDescription = null) },
                    text = { Text("Add expense") },
                )
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

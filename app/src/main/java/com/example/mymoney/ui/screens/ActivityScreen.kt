package com.example.mymoney.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.components.ActivityItem
import com.example.mymoney.ui.components.ActivityRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Mock data until activity is fetched from storage.
private val today: LocalDate = LocalDate.now()
private val mockActivity = listOf(
    ActivityItem("1", Icons.Filled.Restaurant, "Breakfast", "Food & drinks · Cash", "₹180", "Expense", today),
    ActivityItem("2", Icons.Filled.ShoppingCart, "Vegetables", "Groceries · UPI wallet", "₹420", "Expense", today),
    ActivityItem("3", Icons.Filled.CreditCard, "Card bill payment", "Primary bank to Credit card", "₹1,500", "Transfer", today.minusDays(1)),
    ActivityItem("4", Icons.AutoMirrored.Filled.ReceiptLong, "Rahul repaid you", "Loan repayment · Primary bank", "₹2,000", "Repayment", today.minusDays(1)),
    ActivityItem("5", Icons.Filled.Group, "Weekly groceries", "Flatmates · your share ₹1,000", "₹3,000", "Shared total", today.minusDays(3)),
    ActivityItem("6", Icons.Filled.Restaurant, "Dinner out", "Food & drinks · Credit card", "₹950", "Expense", today.minusDays(3)),
    ActivityItem("7", Icons.Filled.ShoppingCart, "Phone recharge", "Bills · UPI wallet", "₹299", "Expense", today.minusDays(5)),
)

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

private fun dayLabel(date: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(dayFormatter)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityScreen(modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTypes by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    val allTypes = remember { mockActivity.map { it.type }.distinct() }
    val grouped = remember(query, selectedTypes) {
        mockActivity
            .filter { selectedTypes.isEmpty() || it.type in selectedTypes }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
            }
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search activity") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
            FilledTonalIconButton(
                onClick = { showFilters = true },
                colors = if (selectedTypes.isEmpty()) {
                    IconButtonDefaults.filledTonalIconButtonColors()
                } else {
                    IconButtonDefaults.filledIconButtonColors()
                },
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
            }
        }

        if (grouped.isEmpty()) {
            Text(
                "No activity found",
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            grouped.forEach { (date, items) ->
                item(key = "header-$date") {
                    Text(
                        text = dayLabel(date),
                        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 6.dp),
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                    ActivityRow(item = item, index = index, count = items.size, onClick = {})
                }
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            allTypes = allTypes,
            selected = selectedTypes,
            onSelectedChange = { selectedTypes = it },
            onDismiss = { showFilters = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    allTypes: List<String>,
    selected: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp),
        ) {
            Text("Filter", style = MaterialTheme.typography.headlineSmallEmphasized)
            Spacer(Modifier.height(16.dp))
            Text(
                "Type",
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allTypes.forEach { type ->
                    FilterChip(
                        selected = type in selected,
                        onClick = {
                            onSelectedChange(if (type in selected) selected - type else selected + type)
                        },
                        label = { Text(type) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onSelectedChange(emptySet()) },
                    modifier = Modifier.weight(1f),
                    enabled = selected.isNotEmpty(),
                    shapes = ButtonDefaults.shapes(),
                ) { Text("Reset") }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shapes = ButtonDefaults.shapes(),
                ) { Text("Done") }
            }
        }
    }
}

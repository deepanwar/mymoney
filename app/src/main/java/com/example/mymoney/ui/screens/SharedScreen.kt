package com.example.mymoney.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.components.ActivityItem
import com.example.mymoney.ui.components.ActivityRow
import com.example.mymoney.ui.components.SectionHeader

private data class SharedGroup(
    val name: String,
    val members: String,
    val label: String,
    val amount: String,
    val owed: Boolean,
)

// Mock data until shared balances are fetched from storage.
private val mockGroups = listOf(
    SharedGroup("Flatmates", "You, Rahul, Amit", "You get back", "₹500", owed = false),
    SharedGroup("Goa trip", "You, Rahul, Priya", "You owe", "₹2,000", owed = true),
)

private val mockSettlements = listOf(
    ActivityItem(
        "s1", Icons.AutoMirrored.Filled.ReceiptLong, "Rahul repaid you",
        "Loan repayment · Primary bank", "₹2,000", "Repayment",
    ),
)

@Composable
private fun positiveColor() =
    if (isSystemInDarkTheme()) Color(0xFF4CD37A) else Color(0xFF1B7F3A)

@Composable
private fun negativeColor() = MaterialTheme.colorScheme.error

@Composable
fun SharedScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    label = "To receive",
                    amount = "₹3,500",
                    icon = Icons.AutoMirrored.Filled.CallReceived,
                    color = positiveColor(),
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = "You owe",
                    amount = "₹2,000",
                    icon = Icons.AutoMirrored.Filled.CallMade,
                    color = negativeColor(),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "Group and loan balances stay separate. Nothing is offset automatically.",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column {
            SectionHeader("Groups", actionLabel = "New group")
            GroupsList(mockGroups)
        }

        Column {
            SectionHeader("Loans", actionLabel = "New loan")
            LoanItem()
        }

        Column {
            SectionHeader("Recent settlements")
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                mockSettlements.forEachIndexed { i, item ->
                    ActivityRow(item, i, mockSettlements.size, onClick = {})
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, contentDescription = null, modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null) }
    }
}

@Composable
private fun StatusAmount(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GroupsList(groups: List<SharedGroup>) {
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        groups.forEachIndexed { i, g ->
            SegmentedListItem(
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(i, groups.size),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                leadingContent = { IconTile(Icons.Filled.Group) },
                supportingContent = { Text(g.members, maxLines = 1) },
                trailingContent = {
                    StatusAmount(g.label, g.amount, if (g.owed) negativeColor() else positiveColor())
                },
            ) { Text(g.name, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoanItem() {
    Spacer(Modifier.height(8.dp))
    SegmentedListItem(
        onClick = {},
        shapes = ListItemDefaults.segmentedShapes(0, 1),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        leadingContent = { IconTile(Icons.Filled.Payments) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Principal ₹5,000 · repaid ₹2,000")
                OutlinedButton(onClick = {}, shapes = ButtonDefaults.shapes()) {
                    Text("Record repayment")
                }
            }
        },
        trailingContent = { StatusAmount("To receive", "₹3,000", positiveColor()) },
        verticalAlignment = Alignment.Top,
    ) { Text("Lent to Rahul", fontWeight = FontWeight.SemiBold) }
}

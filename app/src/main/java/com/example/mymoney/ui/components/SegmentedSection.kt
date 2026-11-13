package com.example.mymoney.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** One row: avatar + name on the left, amount on the right. */
data class AmountItem(
    val id: String,
    val name: String,
    val amount: String,
)

/** Reusable titled, segmented list of [AmountItem]s. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedAmountSection(
    title: String,
    items: List<AmountItem>,
    modifier: Modifier = Modifier,
    onItemClick: (AmountItem) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            items.forEachIndexed { index, item ->
                SegmentedListItem(
                    onClick = { onItemClick(item) },
                    shapes = ListItemDefaults.segmentedShapes(index, items.size),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    leadingContent = { InitialAvatar(item.name) },
                    trailingContent = {
                        Text(
                            text = item.amount,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                ) {
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialAvatar(name: String) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = MaterialShapes.Cookie6Sided.toShape(),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

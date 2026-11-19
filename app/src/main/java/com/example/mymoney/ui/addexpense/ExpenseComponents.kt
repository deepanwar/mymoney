package com.example.mymoney.ui.addexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun MemberAvatar(member: Member, colorIndex: Int, size: Dp = 48.dp) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when (colorIndex.absoluteValue % 3) {
        0 -> colors.primaryContainer to colors.onPrimaryContainer
        1 -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
    Surface(Modifier.size(size), shape = CircleShape, color = bg, contentColor = fg) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                member.name.take(1).uppercase(),
                style = if (size >= 40.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Avatar + name + trailing control, with an inset divider below every row but the last. */
@Composable
fun MemberRow(
    member: Member,
    index: Int,
    showDivider: Boolean,
    onClick: (() -> Unit)? = null,
    supporting: String? = null,
    trailing: @Composable () -> Unit,
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MemberAvatar(member, index)
            Column(Modifier.weight(1f)) {
                Text(member.displayName, style = MaterialTheme.typography.titleMedium)
                if (supporting != null) {
                    Text(
                        supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing()
        }
        if (showDivider) {
            HorizontalDivider(
                Modifier.padding(start = 88.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

/** Compact numeric input used for per-person amounts and percentages. */
@Composable
fun NumberBox(
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    suffix: String? = null,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.matches(moneyInputRegex)) onValueChange(it) },
        modifier = modifier.width(128.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
        placeholder = { Text("0", Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
    )
}

/** Title row for bottom sheets: headline on the left, close button on the right. */
@Composable
fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = if (leading != null) 8.dp else 24.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(
            title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            modifier = Modifier.weight(1f),
        )
        FilledTonalIconButton(
            onClick = onClose,
            modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize()),
        ) { Icon(Icons.Filled.Close, contentDescription = "Close", Modifier.size(18.dp)) }
    }
}

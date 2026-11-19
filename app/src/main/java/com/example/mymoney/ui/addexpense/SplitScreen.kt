package com.example.mymoney.ui.addexpense

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitScreen(
    total: Long,
    members: List<Member>,
    initial: SplitState,
    onBack: () -> Unit,
    onDone: (SplitState) -> Unit,
) {
    var state by remember { mutableStateOf(initial) }
    val owed = state.owed(total, members)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to split") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    SplitSummary(state, total, members, owed, onSelectAll = { all ->
                        state = state.copy(included = if (all) members.map { it.id }.toSet() else emptySet())
                    })
                    Spacer(Modifier.size(16.dp))
                    Button(
                        onClick = { onDone(state) },
                        enabled = state.isValid(total),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
                    ) { Text("Next", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ModeSelector(state.mode) { state = state.copy(mode = it) }
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                AnimatedContent(targetState = state.mode, label = "split-mode") { mode ->
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 20.dp)) {
                        val (title, subtitle) = when (mode) {
                            SplitMode.Equally -> "Split equally" to "Select people to split the amount equally"
                            SplitMode.Amounts -> "Split by exact amounts" to "Enter what each person should pay"
                            SplitMode.Shares -> "Split by shares" to "Divide the amount proportionally by shares"
                            SplitMode.Percentages -> "Split by percentages" to "Divide the total by % share"
                        }
                        Text(title, style = MaterialTheme.typography.titleLargeEmphasized, modifier = Modifier.padding(horizontal = 24.dp))
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        )
                        Spacer(Modifier.size(8.dp))
                        members.forEachIndexed { i, m ->
                            val divider = i < members.lastIndex
                            val share = owed[m.id]?.takeIf { it > 0 }?.let(::formatRupees)
                            when (mode) {
                                SplitMode.Equally -> {
                                    val toggle = {
                                        state = state.copy(included = state.included.toggle(m.id))
                                    }
                                    MemberRow(m, i, divider, onClick = toggle, supporting = share) {
                                        Checkbox(checked = m.id in state.included, onCheckedChange = { toggle() })
                                    }
                                }
                                SplitMode.Amounts -> MemberRow(m, i, divider) {
                                    NumberBox(state.amounts[m.id].orEmpty(), {
                                        state = state.copy(amounts = state.amounts + (m.id to it))
                                    }, prefix = "₹")
                                }
                                SplitMode.Shares -> MemberRow(m, i, divider, supporting = share) {
                                    ShareStepper(state.shares[m.id] ?: 0) {
                                        state = state.copy(shares = state.shares + (m.id to it))
                                    }
                                }
                                SplitMode.Percentages -> MemberRow(m, i, divider, supporting = share) {
                                    NumberBox(state.percents[m.id].orEmpty(), {
                                        state = state.copy(percents = state.percents + (m.id to it))
                                    }, suffix = "%")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Connected expressive button group; scrolls horizontally when it doesn't fit. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeSelector(selected: SplitMode, onSelect: (SplitMode) -> Unit) {
    val modes = SplitMode.entries
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        modes.forEachIndexed { i, mode ->
            ToggleButton(
                checked = selected == mode,
                onCheckedChange = { onSelect(mode) },
                shapes = when (i) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            ) {
                Icon(mode.icon, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(mode.label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShareStepper(value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(
            onClick = { onValueChange(value - 1) },
            enabled = value > 0,
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
        ) { Icon(Icons.Filled.Remove, "Fewer shares") }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp),
        )
        FilledTonalIconButton(
            onClick = { onValueChange(value + 1) },
            enabled = value < 99,
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
        ) { Icon(Icons.Filled.Add, "More shares") }
    }
}

@Composable
private fun SplitSummary(
    state: SplitState,
    total: Long,
    members: List<Member>,
    owed: Map<String, Long>,
    onSelectAll: (Boolean) -> Unit,
) {
    when (state.mode) {
        SplitMode.Equally -> Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val n = state.included.size
                val each = if (n > 0) formatRupees(total / n) else formatRupees(0)
                Text("$each per person", style = MaterialTheme.typography.titleMedium)
                Text(
                    "($n ${if (n == 1) "person" else "people"} selected)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("Select all", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(4.dp))
            Checkbox(checked = state.included.size == members.size, onCheckedChange = onSelectAll)
        }
        SplitMode.Amounts -> {
            val added = owed.values.sum()
            CenterSummary(
                "Amount added ${formatRupees(added)}",
                remainderLine(total - added, ::formatRupees),
                isError = added > total,
            )
        }
        SplitMode.Shares -> {
            val n = state.shares.values.sum()
            CenterSummary("Total $n ${if (n == 1) "share" else "shares"}", null, false)
        }
        SplitMode.Percentages -> {
            val added = state.percents.values.sumOf(::parseMoney)
            CenterSummary(
                "${formatPercent(added)} added",
                remainderLine(100_00L - added, ::formatPercent),
                isError = added > 100_00L,
            )
        }
    }
}

private fun remainderLine(left: Long, format: (Long) -> String) =
    if (left >= 0) "${format(left)} left" else "${format(-left)} over"

@Composable
private fun CenterSummary(title: String, subtitle: String?, isError: Boolean) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Set<String>.toggle(id: String) = if (id in this) this - id else this + id

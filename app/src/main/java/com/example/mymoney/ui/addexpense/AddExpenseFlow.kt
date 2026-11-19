package com.example.mymoney.ui.addexpense

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Placeholder group until groups come from storage.
private const val sampleGroupName = "Flatmates"
private val sampleMembers = listOf(
    Member("me", "Me", isYou = true),
    Member("alex", "Alex"),
    Member("sam", "Sam"),
)

private enum class Sheet { Description, PaidBy }

/** Add-expense flow: amount → description sheet → who paid sheet → how-to-split screen. */
@Composable
fun AddExpenseFlow(
    onClose: () -> Unit,
    onSave: (ExpenseDraft) -> Unit,
    groupName: String = sampleGroupName,
    members: List<Member> = sampleMembers,
) {
    val you = members.firstOrNull { it.isYou } ?: members.first()
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by remember { mutableStateOf<Category?>(null) }
    var payer by remember { mutableStateOf<Payer>(Payer.Single(you.id)) }
    var split by remember { mutableStateOf(SplitState(included = members.map { it.id }.toSet())) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var showSplit by rememberSaveable { mutableStateOf(false) }

    val total = parseMoney(amountText)

    if (showSplit) {
        BackHandler { showSplit = false }
        SplitScreen(
            total = total,
            members = members,
            initial = split,
            onBack = { showSplit = false },
            onDone = { split = it; showSplit = false },
        )
        return
    }

    val paidBy: Map<String, Long> = when (val p = payer) {
        is Payer.Single -> mapOf(p.memberId to total)
        is Payer.Multiple -> p.amounts.mapValues { parseMoney(it.value) }.filterValues { it > 0 }
    }
    val problem = when {
        total > 0 && payer is Payer.Multiple && paidBy.values.sum() != total ->
            "Amounts paid don't add up to ${formatRupees(total)}"
        total > 0 && !split.isValid(total) -> "Split doesn't add up to ${formatRupees(total)}"
        else -> null
    }
    val canSave = total > 0 && description.isNotBlank() && problem == null

    AddExpenseContent(
        groupName = groupName,
        amountText = amountText,
        onAmountChange = { amountText = it },
        description = description,
        category = category,
        payerLabel = when (val p = payer) {
            is Payer.Single -> members.first { it.id == p.memberId }.shortName
            is Payer.Multiple -> "${paidBy.size} people"
        },
        payerMember = (payer as? Payer.Single)?.let { p ->
            members.indexOfFirst { it.id == p.memberId }.let { i -> members[i] to i }
        },
        splitLabel = split.mode.shortLabel,
        problem = problem,
        canSave = canSave,
        onBack = onClose,
        onDescriptionClick = { sheet = Sheet.Description },
        onPaidByClick = { sheet = Sheet.PaidBy },
        onSplitClick = { showSplit = true },
        onSave = {
            onSave(ExpenseDraft(total, description.trim(), category, paidBy, split.owed(total, members), split.mode))
        },
    )

    when (sheet) {
        Sheet.Description -> DescriptionSheet(
            initialText = description,
            initialCategory = category,
            onDismiss = { sheet = null },
            onConfirm = { text, cat -> description = text; category = cat },
        )
        Sheet.PaidBy -> PaidBySheet(
            members = members,
            total = total,
            initial = payer,
            onDismiss = { sheet = null },
            onConfirm = { payer = it },
        )
        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddExpenseContent(
    groupName: String,
    amountText: String,
    onAmountChange: (String) -> Unit,
    description: String,
    category: Category?,
    payerLabel: String,
    payerMember: Pair<Member, Int>?,
    splitLabel: String,
    problem: String?,
    canSave: Boolean,
    onBack: () -> Unit,
    onDescriptionClick: () -> Unit,
    onPaidByClick: () -> Unit,
    onSplitClick: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    Modifier.size(72.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Groups, null, Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("With $groupName", style = MaterialTheme.typography.headlineMediumEmphasized)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter amount to split",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                AmountField(amountText, onAmountChange)
                Spacer(Modifier.height(16.dp))
                DescriptionPill(description, category, onDescriptionClick)
            }

            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Paid by", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SelectorPill(payerLabel, onPaidByClick, leading = payerMember?.let { { MemberAvatar(it.first, it.second, 28.dp) } })
                    Spacer(Modifier.width(4.dp))
                    Text("Split", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SelectorPill(splitLabel, onSplitClick, isError = problem?.startsWith("Split") == true)
                }
                if (problem != null) {
                    Text(
                        problem,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onSave,
                    enabled = canSave,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
                    contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                ) { Text("Add expense", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AmountField(value: String, onValueChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val style = MaterialTheme.typography.displayLargeEmphasized.copy(
        fontSize = 72.sp,
        lineHeight = 80.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "₹",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.offset(y = 12.dp),
        )
        BasicTextField(
            value = value,
            onValueChange = { if (it.matches(moneyInputRegex)) onValueChange(it) },
            textStyle = style,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(IntrinsicSize.Min).focusRequester(focus),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text("0", style = style.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)))
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun DescriptionPill(description: String, category: Category?, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    if (description.isBlank()) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(size.height / 2),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
                )
            },
        ) {
            Text(
                "What is this expense for?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    } else {
        Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(
                Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(listOfNotNull(category?.emoji, description).joinToString(" "), style = MaterialTheme.typography.titleMedium)
                Icon(Icons.Filled.Edit, contentDescription = "Edit description", Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SelectorPill(
    label: String,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
) {
    val border = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
    Surface(onClick = onClick, shape = CircleShape, border = BorderStroke(1.dp, border), color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.padding(start = if (leading != null) 6.dp else 16.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leading?.invoke()
            Text(label, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, Modifier.size(20.dp))
        }
    }
}

/** Hides the sheet with its exit animation, then runs [after]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSheetCloser(state: SheetState, onDismiss: () -> Unit): (after: () -> Unit) -> Unit {
    val scope = rememberCoroutineScope()
    return { after ->
        scope.launch { state.hide() }.invokeOnCompletion {
            after()
            onDismiss()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DescriptionSheet(
    initialText: String,
    initialCategory: Category?,
    onDismiss: () -> Unit,
    onConfirm: (String, Category?) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(state, onDismiss)
    var text by rememberSaveable { mutableStateOf(initialText) }
    var category by remember { mutableStateOf(initialCategory) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val confirm = { close { onConfirm(text.trim().ifBlank { category?.label.orEmpty() }, category) } }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        SheetHeader("What is this expense for?", onClose = { close {} })
        Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("e.g. Coffee, Dinner") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank() || category != null) confirm() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                expenseCategories.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = if (category == c) null else c },
                        label = { Text(c.label) },
                        leadingIcon = { Text(c.emoji) },
                    )
                }
            }
            Button(
                onClick = confirm,
                enabled = text.isNotBlank() || category != null,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight).padding(bottom = 16.dp),
            ) { Text("Next", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaidBySheet(
    members: List<Member>,
    total: Long,
    initial: Payer,
    onDismiss: () -> Unit,
    onConfirm: (Payer) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(state, onDismiss)
    var multiple by remember { mutableStateOf(initial is Payer.Multiple) }
    var selected by remember { mutableStateOf((initial as? Payer.Single)?.memberId ?: members.first().id) }
    val amounts = remember {
        mutableStateMapOf<String, String>().apply { (initial as? Payer.Multiple)?.let { putAll(it.amounts) } }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        SheetHeader(
            "Who paid?",
            onClose = { close {} },
            leading = if (multiple) {
                { IconButton(onClick = { multiple = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Single payer") } }
            } else null,
        )
        AnimatedContent(targetState = multiple, label = "payer-mode") { isMultiple ->
            Column {
                if (!isMultiple) {
                    members.forEachIndexed { i, m ->
                        MemberRow(m, i, showDivider = i < members.lastIndex, onClick = { selected = m.id }) {
                            RadioButton(selected = selected == m.id, onClick = { selected = m.id })
                        }
                    }
                    Surface(
                        onClick = { multiple = true },
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box {
                                members.take(3).forEachIndexed { i, m ->
                                    Box(Modifier.padding(start = (i * 20).dp)) { MemberAvatar(m, i, 36.dp) }
                                }
                            }
                            Text(
                                "Multiple people paid",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f).padding(start = 16.dp),
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                        }
                    }
                } else {
                    members.forEachIndexed { i, m ->
                        MemberRow(m, i, showDivider = i < members.lastIndex) {
                            NumberBox(amounts[m.id].orEmpty(), { amounts[m.id] = it }, prefix = "₹")
                        }
                    }
                    val paid = amounts.values.sumOf(::parseMoney)
                    Column(
                        Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("${formatRupees(paid)} of ${formatRupees(total)}", style = MaterialTheme.typography.titleLarge)
                        val left = total - paid
                        Text(
                            if (left >= 0) "${formatRupees(left)} left" else "${formatRupees(-left)} over",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (left < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        val valid = !multiple || (total > 0 && amounts.values.sumOf(::parseMoney) == total)
        Button(
            onClick = {
                val payer = if (multiple) Payer.Multiple(amounts.toMap()) else Payer.Single(selected)
                close { onConfirm(payer) }
            },
            enabled = valid,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .heightIn(min = ButtonDefaults.MediumContainerHeight),
        ) { Text("Save", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
    }
}

package com.example.mymoney.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

// Default scale plus heavier "emphasized" variants used by expressive components.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography = Typography().let { base ->
    base.copy(
        displayLargeEmphasized = base.displayLarge.emphasized(),
        displayMediumEmphasized = base.displayMedium.emphasized(),
        displaySmallEmphasized = base.displaySmall.emphasized(),
        headlineLargeEmphasized = base.headlineLarge.emphasized(),
        headlineMediumEmphasized = base.headlineMedium.emphasized(),
        headlineSmallEmphasized = base.headlineSmall.emphasized(),
        titleLargeEmphasized = base.titleLarge.emphasized(),
        titleMediumEmphasized = base.titleMedium.emphasized(),
        titleSmallEmphasized = base.titleSmall.emphasized(),
        labelLargeEmphasized = base.labelLarge.emphasized(),
        labelMediumEmphasized = base.labelMedium.emphasized(),
        labelSmallEmphasized = base.labelSmall.emphasized(),
        bodyLargeEmphasized = base.bodyLarge.emphasized(),
        bodyMediumEmphasized = base.bodyMedium.emphasized(),
        bodySmallEmphasized = base.bodySmall.emphasized(),
    )
}

private fun TextStyle.emphasized() = copy(fontWeight = FontWeight.SemiBold)

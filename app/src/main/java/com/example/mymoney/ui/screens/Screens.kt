package com.example.mymoney.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.components.ActivityItem
import com.example.mymoney.ui.components.AmountItem
import com.example.mymoney.ui.components.RecentActivitySection
import com.example.mymoney.ui.components.SegmentedAmountSection

// Blank placeholders. Fill in per feature.
@Composable
fun OverviewScreen(modifier: Modifier = Modifier) {
    // Placeholder data until accounts come from storage.
    val accounts = listOf(
        AmountItem("cash", "Cash", "₹0.00"),
        AmountItem("bank", "Bank Account", "₹0.00"),
        AmountItem("card", "Credit Card", "₹0.00"),
    )
    // Mock data until activity is fetched from storage.
    val recent = listOf(
        ActivityItem("1", Icons.Filled.Restaurant, "Breakfast", "Food & drinks · Cash", "₹180", "Expense"),
        ActivityItem("2", Icons.Filled.ShoppingCart, "Vegetables", "Groceries · UPI wallet", "₹420", "Expense"),
        ActivityItem("3", Icons.Filled.CreditCard, "Card bill payment", "Primary bank to Credit card", "₹1,500", "Transfer"),
        ActivityItem("4", Icons.AutoMirrored.Filled.ReceiptLong, "Rahul repaid you", "Loan repayment · Primary bank", "₹2,000", "Repayment"),
        ActivityItem("5", Icons.Filled.Group, "Weekly groceries", "Flatmates · your share ₹1,000", "₹3,000", "Shared total"),
    )
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SegmentedAmountSection(title = "Accounts", items = accounts)
        RecentActivitySection(title = "Recent activity", items = recent)
    }
}

@Composable
fun InsightScreen(modifier: Modifier = Modifier) { Box(modifier.fillMaxSize()) }

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) { Box(modifier.fillMaxSize()) }

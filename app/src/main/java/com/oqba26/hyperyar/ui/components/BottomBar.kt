package com.oqba26.hyperyar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqba26.hyperyar.util.toPersianDigits

@Composable
fun AppBottomBar(
    currentScreen: String = "products",
    cartItemCount: Int = 0,
    isAdmin: Boolean = true,
    onNavigate: (String) -> Unit,
    onShowCart: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RowNavigationItem(
                icon = Icons.Default.Inventory,
                label = "محصولات",
                selected = currentScreen == "products",
                onClick = { onNavigate("products") }
            )

            RowNavigationItem(
                icon = Icons.Default.People,
                label = "مشتریان",
                selected = currentScreen == "customers",
                onClick = { onNavigate("customers") }
            )

            if (isAdmin) {
                RowNavigationItem(
                    icon = Icons.Default.BarChart,
                    label = "گزارشات",
                    selected = currentScreen == "reports",
                    onClick = { onNavigate("reports") }
                )
            }

            RowNavigationItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = "حسابداری",
                selected = currentScreen == "accounting",
                onClick = { onNavigate("accounting") }
            )

            RowNavigationItem(
                icon = Icons.Default.History,
                label = "تاریخچه",
                selected = currentScreen == "history",
                onClick = { onNavigate("history") }
            )

            RowNavigationItem(
                icon = Icons.Default.ShoppingCart,
                label = "سبد",
                selected = false,
                onClick = onShowCart,
                badgeCount = cartItemCount
            )

            RowNavigationItem(
                icon = Icons.Default.Settings,
                label = "تنظیمات",
                selected = currentScreen == "settings",
                onClick = { onNavigate("settings") }
            )
        }
    }
}

@Composable
private fun RowScope.RowNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val indicatorColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) 
        else Color.Transparent,
        label = "color"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(56.dp)
                .clip(CircleShape)
                .background(indicatorColor),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) { Text(text = badgeCount.toString().toPersianDigits()) }
                    }
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1
        )
    }
}

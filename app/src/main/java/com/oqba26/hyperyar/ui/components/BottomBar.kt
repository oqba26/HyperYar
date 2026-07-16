package com.oqba26.hyperyar.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oqba26.hyperyar.util.toPersianDigits

@Composable
fun AppBottomBar(
    currentScreen: String = "products",
    cartItemCount: Int = 0,
    isAdmin: Boolean = true,
    onNavigate: (String) -> Unit,
    onShowCart: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        val items = remember(isAdmin) {
            listOf(
                NavigationItemData("محصولات", Icons.Default.Inventory, "products"),
                NavigationItemData("مشتریان", Icons.Default.People, "customers"),
                NavigationItemData("حسابداری", Icons.Default.AccountBalanceWallet, "accounting"),
                NavigationItemData("گزارشات", Icons.Default.BarChart, "reports", isAdminOnly = true),
                NavigationItemData("سبد", Icons.Default.ShoppingCart, "cart"),
                NavigationItemData("تنظیمات", Icons.Default.Settings, "settings")
            ).filter { !it.isAdminOnly || isAdmin }
        }

        items.forEach { item ->
            val isSelected = currentScreen == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (item.route == "cart") onShowCart()
                    else onNavigate(item.route)
                },
                icon = {
                    val badgeCount = if (item.route == "cart") cartItemCount else 0
                    if (badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ) { Text(text = badgeCount.toString().toPersianDigits()) }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = Color.White,
                    indicatorColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

private data class NavigationItemData(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isAdminOnly: Boolean = false
)

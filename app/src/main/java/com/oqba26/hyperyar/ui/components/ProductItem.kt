package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.oqba26.hyperyar.data.Product
import com.oqba26.hyperyar.util.toPersianNumber
import com.oqba26.hyperyar.util.toPersianPrice

@Composable
fun ProductItem(
    product: Product,
    isPurchaseMode: Boolean = false,
    isAdmin: Boolean = true,
    shopName: String = "",
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddToCart: () -> Unit,
    onAddBulkToCart: () -> Unit = {},
    onPrintLabel: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onWaste: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            onClick = onClick,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "موجودی: ${product.stock.toPersianNumber()} ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (product.stock > 10) Color.Gray else Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (product.isFavorite) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.sellPrice.toPersianPrice(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAddToCart) {
                        Icon(
                            imageVector = if (isPurchaseMode) Icons.Default.AddBusiness else Icons.Default.AddShoppingCart,
                            contentDescription = "Add",
                            tint = if (isPurchaseMode) Color(0xFFE91E63) else MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (isPurchaseMode && product.unitsInPack > 1) {
                                DropdownMenuItem(
                                    text = { Text("افزودن عمده (${product.unitsInPack.toPersianNumber()} عدد)") },
                                    leadingIcon = { Icon(Icons.Default.Inventory, null, tint = Color(0xFFE91E63)) },
                                    onClick = {
                                        showMenu = false
                                        onAddBulkToCart()
                                    }
                                )
                                HorizontalDivider()
                            }
                            if (isAdmin) {
                                DropdownMenuItem(
                                    text = { Text("ویرایش") },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("چاپ لیبل قیمت") },
                                leadingIcon = { Icon(Icons.Default.Print, null) },
                                onClick = {
                                    showMenu = false
                                    onPrintLabel()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ثبت ضایعات") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = {
                                    showMenu = false
                                    onWaste()
                                }
                            )
                            if (isAdmin) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("حذف کالا", color = Color.Red) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

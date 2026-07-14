package com.oqba26.hyperyar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddToCart: () -> Unit,
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
                    Text(
                        text = "موجودی: ${product.stock.toPersianNumber()} ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stock > 10) Color.Gray else Color.Red
                    )
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
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add", tint = MaterialTheme.colorScheme.secondary)
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("ویرایش") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
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

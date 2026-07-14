package com.oqba26.hyperyar.util

import android.content.Context
import android.content.Intent
import com.oqba26.hyperyar.data.InvoiceWithItems

object PrintHelper {
    fun generateReceiptText(invoiceWithItems: InvoiceWithItems, shopName: String): String {
        val sb = StringBuilder()
        sb.append("      $shopName      \n")
        sb.append("--------------------------------\n")
        sb.append("فاکتور شماره: ${invoiceWithItems.invoice.id}\n")
        sb.append("تاریخ: ${invoiceWithItems.invoice.timestamp.toPersianDateTimeString()}\n")
        sb.append("--------------------------------\n")
        sb.append("نام کالا        تعداد    قیمت\n")
        
        invoiceWithItems.items.forEach { item ->
            val name = if (item.productName.length > 14) item.productName.take(12) + ".." else item.productName.padEnd(14)
            sb.append("$name  ${item.quantity.toPersianNumber()}  ${item.priceAtSale.toPersianPrice()}\n")
        }
        
        sb.append("--------------------------------\n")
        sb.append("مجموع کل: ${invoiceWithItems.invoice.totalAmount.toPersianPrice()}\n")
        sb.append("تخفیف: ${invoiceWithItems.invoice.totalDiscount.toPersianPrice()}\n")
        sb.append("مبلغ پرداختی: ${invoiceWithItems.invoice.amountPaid.toPersianPrice()}\n")
        sb.append("--------------------------------\n")
        sb.append("   از خرید شما متشکریم   \n")
        
        return sb.toString()
    }

    fun sendToPrinter(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "چاپ فاکتور")
        context.startActivity(shareIntent)
    }
}

package com.oqba26.hyperyar.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.oqba26.hyperyar.data.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream

object InvoicePdfHelper {

    fun generateAndShareInvoice(
        context: Context, 
        invoiceWithItems: InvoiceWithItems,
        shopName: String = "",
        shopPhone: String = "",
        shopAddress: String = "",
        customerName: String? = null,
        customerPhone: String? = null
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { strokeWidth = 1f }
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val textPaint = Paint().apply {
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val footerPaint = Paint().apply {
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }

        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        var currentY = 60f

        val fullTitle = if (shopName.isNotBlank()) "فروشگاه $shopName" else "هایپریار"
        canvas.drawText(fullTitle, pageWidth / 2, currentY, titlePaint)
        
        currentY = 110f 

        canvas.drawText("شماره فاکتور: ${invoiceWithItems.invoice.id.toString().toPersianDigits()}", pageWidth - margin, currentY, textPaint)
        currentY += 20f
        canvas.drawText("تاریخ: ${invoiceWithItems.invoice.timestamp.toPersianDateTimeString()}", pageWidth - margin, currentY, textPaint)
        
        if (!customerName.isNullOrBlank()) {
            currentY += 20f
            canvas.drawText("مشتری: $customerName", pageWidth - margin, currentY, textPaint)
            if (!customerPhone.isNullOrBlank()) {
                currentY += 20f
                canvas.drawText("تلفن: ${customerPhone.toPersianDigits()}", pageWidth - margin, currentY, textPaint)
            }
        }
        
        currentY += 40f

        val colQty = margin + 150f
        val colName = pageWidth - margin - 50f
        
        canvas.drawText("ردیف", pageWidth - margin, currentY, headerPaint)
        canvas.drawText("نام کالا", colName, currentY, headerPaint)
        canvas.drawText("تعداد", colQty, currentY, headerPaint)
        canvas.drawText("جمع", margin + 40f, currentY, headerPaint)
        
        currentY += 10f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 25f

        invoiceWithItems.items.forEachIndexed { index, item ->
            canvas.drawText((index + 1).toString().toPersianDigits(), pageWidth - margin, currentY, textPaint)
            canvas.drawText(item.productName, colName, currentY, textPaint)
            canvas.drawText(item.quantity.toPersianNumber(), colQty, currentY, textPaint)
            canvas.drawText((item.quantity * item.priceAtSale).toPersianPrice(), margin + 40f, currentY, textPaint)
            currentY += 25f
        }

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 30f

        canvas.drawText("جمع کل فاکتور: ${invoiceWithItems.invoice.totalAmount.toPersianPrice()}", pageWidth - margin, currentY, headerPaint)
        currentY += 25f
        canvas.drawText("مبلغ پرداختی: ${invoiceWithItems.invoice.amountPaid.toPersianPrice()}", pageWidth - margin, currentY, textPaint)
        
        val remaining = invoiceWithItems.invoice.totalAmount - invoiceWithItems.invoice.amountPaid
        if (remaining > 0) {
            currentY += 25f
            canvas.drawText("مانده بدهی: ${remaining.toPersianPrice()}", pageWidth - margin, currentY, textPaint.apply { 
                color = Color.RED 
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            })
        }

        currentY = pageInfo.pageHeight - 100f
        if (shopAddress.isNotBlank()) canvas.drawText("آدرس: $shopAddress", pageWidth - margin, currentY, footerPaint)
        if (shopPhone.isNotBlank()) canvas.drawText("تلفن: ${shopPhone.toPersianDigits()}", pageWidth - margin, currentY + 18f, footerPaint)

        pdfDocument.finishPage(page)

        val fileName = "Invoice_${invoiceWithItems.invoice.id}.pdf"
        val file = File(context.cacheDir, fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
    }
}

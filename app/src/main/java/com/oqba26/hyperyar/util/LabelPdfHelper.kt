package com.oqba26.hyperyar.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.oqba26.hyperyar.data.Product
import java.io.File
import java.io.FileOutputStream

object LabelPdfHelper {

    fun generateAndShareLabel(context: Context, product: Product, shopName: String = "") {
        val pdfDocument = PdfDocument()
        
        // Label Size: approx 50mm x 30mm (141 x 85 points at 72dpi)
        val pageInfo = PdfDocument.PageInfo.Builder(150, 90, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val paint = Paint()
        
        // Draw border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        canvas.drawRect(5f, 5f, 145f, 85f, paint)
        
        paint.style = Paint.Style.FILL
        
        // Shop Name
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(shopName.take(20), 10f, 18f, paint)
        
        // Product Name
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val nameText = if (product.name.length > 18) product.name.take(16) + ".." else product.name
        canvas.drawText(nameText, 10f, 35f, paint)
        
        // Price
        paint.textSize = 14f
        paint.color = Color.RED
        val priceText = product.sellPrice.toPersianPrice()
        canvas.drawText(priceText, 10f, 55f, paint)
        
        // Barcode (Text representation)
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        if (product.barcode.isNotBlank()) {
            canvas.drawText(product.barcode.toPersianDigits(), 10f, 75f, paint)
            
            // Simple visual lines to simulate barcode
            paint.strokeWidth = 1f
            var startX = 80f
            for (i in 0 until 10) {
                val lineW = (1..3).random().toFloat()
                canvas.drawLine(startX, 65f, startX, 80f, paint)
                startX += lineW + 1f
            }
        }

        pdfDocument.finishPage(page)

        val fileName = "Label_${product.id}.pdf"
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
        context.startActivity(Intent.createChooser(intent, "چاپ لیبل"))
    }
}

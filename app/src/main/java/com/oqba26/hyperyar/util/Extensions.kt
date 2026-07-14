package com.oqba26.hyperyar.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

fun String.toPersianDigits(): String {
    var result = this
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in 0..9) {
        result = result.replace(i.toString(), persianDigits[i])
    }
    return result
}

fun String.fromPersianDigits(): String {
    var result = this
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in 0..9) {
        result = result.replace(persianDigits[i], i.toString())
    }
    return result
}

fun String.cleanNumber(): String {
    return this.replace(",", "").replace("٫", "").fromPersianDigits()
}

fun Double.toPersianPrice(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this).toPersianDigits() + " تومان"
}

fun Number.toPersianNumber(): String {
    val formatter = NumberFormat.getInstance(Locale("fa", "IR"))
    return if ((this is Double) && (this == this.toLong().toDouble())) {
        formatter.format(this.toLong())
    } else {
        formatter.format(this)
    }
}

fun Long.toPersianDateTimeString(): String {
    val pDate = PersianDate(this)
    val pDateFormat = PersianDateFormat("Y/m/d H:i")
    return pDateFormat.format(pDate).toPersianDigits()
}

fun Long.toPersianDateString(): String {
    val pDate = PersianDate(this)
    val pDateFormat = PersianDateFormat("Y/m/d")
    return pDateFormat.format(pDate).toPersianDigits()
}

class PersianNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val hasDot = originalText.contains(".")
        val formatted = if (hasDot) originalText.toPersianDigits() else formatWithCommas(originalText).toPersianDigits()

        val offsetMapping = if (hasDot) {
            OffsetMapping.Identity
        } else {
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    val len = originalText.length
                    var commasBefore = 0
                    for (i in 1 until offset) {
                        if ((len - i) % 3 == 0) commasBefore++
                    }
                    return offset + commasBefore
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 0) return 0
                    val len = originalText.length
                    var digitsFound = 0
                    var currentOffset = 0
                    while (currentOffset < offset && digitsFound < len) {
                        val isComma = (len - digitsFound) % 3 == 0 && digitsFound != 0
                        if (isComma) {
                            currentOffset++
                            if (currentOffset > offset) break
                        }
                        digitsFound++
                        currentOffset++
                    }
                    return digitsFound
                }
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping,
        )
    }

    private fun formatWithCommas(s: String): String {
        if (s.isEmpty()) return ""
        val isNegative = s.startsWith("-")
        val cleanString = if (isNegative) s.substring(1) else s
        
        val sb = StringBuilder()
        if (isNegative) sb.append("-")
        
        for (i in cleanString.indices) {
            sb.append(cleanString[i])
            val digitsFromRight = cleanString.length - 1 - i
            if (digitsFromRight > 0 && digitsFromRight % 3 == 0) {
                sb.append(",")
            }
        }
        return sb.toString()
    }
}

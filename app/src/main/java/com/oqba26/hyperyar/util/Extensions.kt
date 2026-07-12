package com.oqba26.hyperyar.util

import java.text.DecimalFormat

fun String.toPersianDigits(): String {
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    return this.map { it.toString().toIntOrNull()?.let { persianDigits[it] } ?: it }.joinToString("")
}

fun Double.toPersianPrice(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this).toPersianDigits() + " تومان"
}

fun Double.toPersianNumber(): String {
    return this.toString().toPersianDigits()
}

fun Int.toPersianNumber(): String {
    return this.toString().toPersianDigits()
}

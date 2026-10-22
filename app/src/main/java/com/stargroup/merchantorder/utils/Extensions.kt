package com.stargroup.merchantorder.utils

import com.stargroup.merchantorder.data.model.response.Transaction
import java.text.NumberFormat
import java.util.Locale

fun List<Transaction>.countByStatus(status: String): Int {
    return count { it.orderStatus.equals(status, ignoreCase = true) }
}

/**
 * Extension format Rupiah
 */
fun Int.toRupiah(): String {
    return NumberFormat
        .getCurrencyInstance(Locale("in", "ID"))
        .format(this)
}
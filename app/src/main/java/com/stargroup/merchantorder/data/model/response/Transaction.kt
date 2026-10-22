package com.stargroup.merchantorder.data.model.response

import android.os.Parcelable
import com.stargroup.merchantorder.data.model.entity.DetailItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val transactionId: Int,
    val invoiceNumber: String,
    val nomorHp: String?,
    val customer: String,
    val table: String,
    val details: List<DetailItem>,
    val grandTotal: Int,
    val discount: Int,
    val paymentMethod: String,
    val paymentProof: String?,
    val orderStatus: String,
    val addTime: String,
    val qrCode: String
): Parcelable


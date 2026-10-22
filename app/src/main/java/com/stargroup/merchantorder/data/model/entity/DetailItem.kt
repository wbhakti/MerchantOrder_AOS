package com.stargroup.merchantorder.data.model.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailItem(
    val menuId: String,
    val note: String,
    val quantity: Int,
    val price: Int,
    val productDiscount: Int
): Parcelable
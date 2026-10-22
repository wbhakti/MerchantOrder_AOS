package com.stargroup.merchantorder.data.model.request

data class UpdateStatusRequest(
    val id_transaksi: Int,
    val status: String,
    val token: String
)
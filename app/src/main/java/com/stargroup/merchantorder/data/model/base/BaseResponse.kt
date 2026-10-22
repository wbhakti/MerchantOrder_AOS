package com.stargroup.merchantorder.data.model.base

data class BaseResponse<T>(
    val endpoint: String,
    val responseCode: String,
    val responseMessage: String,
    val data: T?
)
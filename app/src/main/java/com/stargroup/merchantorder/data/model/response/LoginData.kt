package com.stargroup.merchantorder.data.model.response

data class LoginData(
    val id: Int,
    val username: String,
    val role: String,
    val token: String
)
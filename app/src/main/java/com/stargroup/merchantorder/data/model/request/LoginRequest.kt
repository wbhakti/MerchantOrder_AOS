package com.stargroup.merchantorder.data.model.request

data class LoginRequest(
    val username: String,
    val password: String,
    val fcm_token: String,
    val device: String
)
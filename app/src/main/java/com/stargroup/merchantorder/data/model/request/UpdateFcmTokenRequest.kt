package com.stargroup.merchantorder.data.model.request

class UpdateFcmTokenRequest (
    val user_id: String,
    val fcm_token: String,
    val device: String,
    val token: String
)
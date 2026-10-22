package com.stargroup.merchantorder.data.repository

import com.stargroup.merchantorder.data.model.base.BaseResponse
import com.stargroup.merchantorder.data.model.request.LoginRequest
import com.stargroup.merchantorder.data.model.request.UpdateFcmTokenRequest
import com.stargroup.merchantorder.data.model.response.LoginData
import com.stargroup.merchantorder.data.remote.RetrofitClient
import retrofit2.Response

class UserRepository {

    suspend fun login(
        username: String,
        password: String,
        fcm_token: String,
        device: String
    ): Response<BaseResponse<LoginData>> {

        return RetrofitClient.api.login(
            "530a82978dbea5dc25366eaa42fa6898517e9e6489cd02d60420190ca7fd4dfa",
            LoginRequest(
                username = username,
                password = password,
                fcm_token = fcm_token,
                device = device,
            )
        )
    }

    suspend fun setFcmToken(
        user_id: String,
        fcm_token: String,
        device: String,
        token: String,

        ): Response<BaseResponse<String>> {
        return RetrofitClient.api.updateFcmToken(
            "530a82978dbea5dc25366eaa42fa6898517e9e6489cd02d60420190ca7fd4dfa",
            UpdateFcmTokenRequest(
                user_id = user_id,
                fcm_token = fcm_token,
                device = device,
                token = token
            )
        )
    }
}
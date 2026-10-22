package com.stargroup.merchantorder.data.remote

import com.stargroup.merchantorder.data.model.base.BaseResponse
import com.stargroup.merchantorder.data.model.request.LoginRequest
import com.stargroup.merchantorder.data.model.request.TransactionRequest
import com.stargroup.merchantorder.data.model.request.UpdateFcmTokenRequest
import com.stargroup.merchantorder.data.model.request.UpdateStatusRequest
import com.stargroup.merchantorder.data.model.response.LoginData
import com.stargroup.merchantorder.data.model.response.Transaction
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface ApiService {

    @POST("user/login")
    suspend fun login(
        @Header("X-API-Key") apiKey: String,
        @Body request: LoginRequest): Response<BaseResponse<LoginData>>

    @POST("transaction/history")
    suspend fun getTransactions(
        @Header("X-API-Key") apiKey: String,
        @Body request: TransactionRequest
    ): Response<BaseResponse<List<Transaction>>>

    @POST("transaction/updateStatus")
    suspend fun updateStatus(
        @Header("X-API-Key") apiKey: String,
        @Body request: UpdateStatusRequest
    ): Response<BaseResponse<Transaction>>

    @POST("user/save-fcm-token")
    suspend fun updateFcmToken(
        @Header("X-API-Key") apiKey: String,
        @Body request: UpdateFcmTokenRequest
    ): Response<BaseResponse<String>>
}
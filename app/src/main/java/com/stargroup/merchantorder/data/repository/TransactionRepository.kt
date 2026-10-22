package com.stargroup.merchantorder.data.repository

import com.stargroup.merchantorder.data.model.base.BaseResponse
import com.stargroup.merchantorder.data.model.request.TransactionRequest
import com.stargroup.merchantorder.data.model.request.UpdateStatusRequest
import com.stargroup.merchantorder.data.model.response.Transaction
import com.stargroup.merchantorder.data.remote.RetrofitClient
import retrofit2.Response

class TransactionRepository {

    suspend fun getTransactions(
        token: String
    ): Response<BaseResponse<List<Transaction>>> {
        return RetrofitClient.api.getTransactions(
            "530a82978dbea5dc25366eaa42fa6898517e9e6489cd02d60420190ca7fd4dfa",
            TransactionRequest(
                token = token
            )
        )
    }

    suspend fun setStatus(
        token: String,
        idTransaksi: Int,
        status: String,

        ): Response<BaseResponse<Transaction>> {
        return RetrofitClient.api.updateStatus(
            "530a82978dbea5dc25366eaa42fa6898517e9e6489cd02d60420190ca7fd4dfa",
            UpdateStatusRequest(
                token = token,
                id_transaksi = idTransaksi,
                status = status
            )
        )
    }
}
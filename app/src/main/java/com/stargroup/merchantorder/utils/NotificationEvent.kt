package com.stargroup.merchantorder.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationEvent {

    private val _newOrder = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )

    val newOrder = _newOrder.asSharedFlow()

    fun notifyNewOrder(idTransaksi: String) {
        _newOrder.tryEmit(idTransaksi)
    }
}
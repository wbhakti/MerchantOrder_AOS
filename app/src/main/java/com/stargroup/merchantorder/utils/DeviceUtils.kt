package com.stargroup.merchantorder.utils

import android.os.Build

object DeviceUtils {

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar {
            it.uppercase()
        }

        return if (
            Build.MODEL.startsWith(manufacturer, ignoreCase = true)
        ) {
            Build.MODEL
        } else {
            "$manufacturer ${Build.MODEL}"
        }
    }
}
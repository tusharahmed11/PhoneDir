package com.example.phonedir.utils

import android.content.Context
import androidx.core.content.PermissionChecker


object Utils {

    fun durationFormat(duration: String) : String{
        var durationFormatted: String? = null
        durationFormatted = if (duration.toInt() < 60) {
            "$duration sec"
        } else {
            val min = duration.toInt() / 60
            val sec = duration.toInt() % 60
            if (sec == 0) "$min min" else "$min min $sec sec"
        }
        return durationFormatted
    }

     fun checkPermissions(permissions: Array<String>, context: Context): Boolean {
        for (permission in permissions) {
            if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                return false // one or more permissions are not granted
            }
        }
        return true // all permissions are granted
    }

}
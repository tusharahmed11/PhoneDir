package com.example.phonedir.utils

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.data.model.MessageLogModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


object Utils {

    fun durationFormat(duration: String): String {
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
            if (PermissionChecker.checkSelfPermission(
                    context,
                    permission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                return false // one or more permissions are not granted
            }
        }
        return true // all permissions are granted
    }

    fun isInBackground(): Boolean {
        val myProcess = RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(myProcess)
        val isInBackground = myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        Log.d(
            "isInBackground",
            myProcess.processName + " " + myProcess.importance + " " + isInBackground
        )
        return isInBackground
    }

    fun getRecentCallLog(callLogArrayList: ArrayList<CallLogModel>): List<CallLogModel>  {
        if (callLogArrayList.isEmpty()) return emptyList()

        val mostRecentCall = callLogArrayList.maxByOrNull { it.callDateInLong }

        return if (mostRecentCall != null) {
            listOf(mostRecentCall)
        } else {
            emptyList()
        }
    }

    fun getRecentSmsLog(smsLogArrayList: ArrayList<MessageLogModel>): List<MessageLogModel>  {
        val currentDateTime = LocalDateTime.now()
        val twoMinutesAgo = currentDateTime.minus(2, ChronoUnit.MINUTES)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return smsLogArrayList.filter { smsLog ->
            val callDateTime = LocalDateTime.parse("${smsLog.messageDate} ${smsLog.messageTime}", formatter)
            callDateTime.isAfter(twoMinutesAgo) && callDateTime.isBefore(currentDateTime)
        }.take(1)
    }

    fun getSimNumber(context: Context): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            telephonyManager.line1Number
        } else {
            null
        }

       /* val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
             subscriptionManager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID)
        } else {
            null
        }*/
    }
}


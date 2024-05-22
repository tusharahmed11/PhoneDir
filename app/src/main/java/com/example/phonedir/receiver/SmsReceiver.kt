package com.example.phonedir.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null){
            if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val smsStatus = intent.getStringExtra(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
                val statusIntent = Intent("smsStatus")
                statusIntent.putExtra("status", smsStatus)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(statusIntent)
            }

        }
    }
}
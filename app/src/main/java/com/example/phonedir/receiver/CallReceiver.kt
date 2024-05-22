package com.example.phonedir.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null){

    /*        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK){
                Toast.makeText(context, "phone call started", Toast.LENGTH_SHORT).show()
            }else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE){
                Toast.makeText(context, "phone call ended", Toast.LENGTH_SHORT).show()
            }else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING){
                Toast.makeText(context, "phone call ringing", Toast.LENGTH_SHORT).show()
            }*/

            val callStatus = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val statusIntent = Intent("callStatus")
            statusIntent.putExtra("status", callStatus)
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(statusIntent)
        }
    }
}
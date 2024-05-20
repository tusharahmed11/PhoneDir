package com.example.phonedir.views

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonedir.CallLogAdapter
import com.example.phonedir.CallLogModel
import com.example.phonedir.R
import com.example.phonedir.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var callLogArrayList: ArrayList<CallLogModel> = arrayListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var callLogAdapter: CallLogAdapter

    // Define a constant array of the permissions you need
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_PHONE_STATE
    )

    // Register a callback for the permission request result
    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // Check if all permissions are granted
        val allGranted = result.values.all { it }
        if (allGranted) {
            // Call your function that needs the permissions
            fetchCallLog()
        } else {
            // Show a message that some permissions are not granted
            Toast.makeText(this, "Some permissions are not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "callStatus") {
                val callStatus = intent.getStringExtra("status")
                // Now you have the call status, do something with it
                when (callStatus) {
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        Toast.makeText(context, "Phone call started", Toast.LENGTH_SHORT).show()
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        Toast.makeText(context, "Phone call ended", Toast.LENGTH_SHORT).show()
                        fetchCallLog()
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        Toast.makeText(context, "Phone call ringing", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Call Logs"



        initUI()

    }

    override fun onPause() {
        super.onPause()
     /*   val packageName = applicationContext.packageName // Get your project's package name
        val mainActivityClassName = MainActivity::javaClass.name
        val componentName = ComponentName(packageName,mainActivityClassName)
        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP)*/
    }

    private fun initUI() {

        val filter = IntentFilter("callStatus")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        recyclerView = findViewById(R.id.main_rv)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        callLogAdapter = CallLogAdapter(callLogArrayList)
        recyclerView.adapter = callLogAdapter

        if (checkPermissions()){
            fetchCallLog()
        }else{
            // Request the permissions from the user
            permissionResultLauncher.launch(PERMISSIONS)
        }



    }

    private fun fetchCallLog(){
        val sortOrder: String = CallLog.Calls.DATE + " DESC"

        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            sortOrder
        )
        //clearing the arraylist
        callLogArrayList.clear()

        if (cursor != null && cursor.moveToFirst()) {

            val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val contactNumberIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)

            do {
                val callID = cursor.getString(idIndex)
                val phoneNumber = cursor.getString(numberIndex)
                val callDate = cursor.getString(dateIndex)
                val callDuration = cursor.getString(durationIndex)
                var callType = cursor.getString(typeIndex)
                var contactName = cursor.getString(contactNumberIndex)
                contactName = if (contactName == null || contactName.equals("")) "Unknown" else contactName

                val dateFormatter = SimpleDateFormat(
                    "dd MMM yyyy", Locale.getDefault()
                )

                val str_call_date = dateFormatter.format(Date(callDate.toLong()))

                val timeFormatter = SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()
                )
                val str_call_time = timeFormatter.format(Date(callDate.toLong()))
                val str_call_duration = Utils.durationFormat(callDuration)

                callType = when (callType.toInt()) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    CallLog.Calls.VOICEMAIL_TYPE -> "Voicemail"
                    CallLog.Calls.REJECTED_TYPE -> "Rejected"
                    CallLog.Calls.BLOCKED_TYPE -> "Blocked"
                    CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> "Externally Answered"
                    else -> "NA"
                }

                // Check if column index is -1, indicating the column doesn't exist
                if (idIndex != -1) {
                    // Process call history data here
                    Log.d("CallLog", "Call ID: $callID, Phone Number: $phoneNumber, Call Date: $callDate, Call Duration: $callDuration, Call Type: $callType")

                    val callLogModel = CallLogModel(
                        phoneNumber = phoneNumber,
                        contactName = contactName,
                        callType = callType,
                        callDate = str_call_date,
                        callTime = str_call_time,
                        callDuration = str_call_duration
                    )
                    callLogArrayList.add(callLogModel)
                } else {
                    // Handle the case where the column doesn't exist

                }
            } while (cursor.moveToNext())
            cursor.close()

            callLogAdapter.notifyDataSetChanged()
        }

    }


    private fun checkPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if (PermissionChecker.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
                return false // one or more permissions are not granted
            }
        }
        return true // all permissions are granted
    }
}
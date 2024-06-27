package com.example.phonedir.views

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phonedir.PhoneDirWidget
import com.example.phonedir.adapter.CallLogAdapter
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.R
import com.example.phonedir.adapter.SmsLogAdapter
import com.example.phonedir.data.TeleTypeEnum
import com.example.phonedir.data.model.MessageLogModel
import com.example.phonedir.data.model.PhoneDataSubmitModel
import com.example.phonedir.data.model.SubmitDataList
import com.example.phonedir.databinding.ActivityMainBinding
import com.example.phonedir.service.BackgroundApiService
import com.example.phonedir.utils.Utils
import com.example.phonedir.utils.Utils.checkPermissions
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var callLogArrayList: ArrayList<CallLogModel> = arrayListOf()
    private var smsLogArrayList: ArrayList<MessageLogModel> = arrayListOf()
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var smsLogAdapter: SmsLogAdapter
    private var teleType = TeleTypeEnum.CALL

    // Define a constant array of the permissions you need
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.RECEIVE_SMS,
    )

    // Register a callback for the permission request result
    private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // Check if all permissions are granted
        val allGranted = result.values.all { it }
        if (allGranted) {
            // Call your function that needs the permissions
            fetchCallLog()
            fetchSMSLog()
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
                        updateAppWidget("Phone call ended")
                        val phoneDataSubmitList : ArrayList<PhoneDataSubmitModel> = arrayListOf()
                        if (callLogArrayList.isNotEmpty()){
                            callLogArrayList.forEach {
                                val phoneDataSubmitModel = PhoneDataSubmitModel(
                                    direction = it.callType,
                                    duration = it.callDuration,
                                    local_number = it.phoneNumber,
                                    remote_number = it.contactName,
                                    status = it.callType,
                                    type = "call"
                                )

                                phoneDataSubmitList.add(phoneDataSubmitModel)
                            }
                        }
                        val gson = Gson()
                        val jsonData = gson.toJson(SubmitDataList(submitList = phoneDataSubmitList))

                        val serviceIntent = Intent(context, BackgroundApiService::class.java).apply {
                            putExtra("data", jsonData)
                        }
                        context?.startService(serviceIntent)
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        Toast.makeText(context, "Phone call ringing", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "smsStatus"){
                val smsStatus = intent.getStringExtra("status")
                Toast.makeText(context, "Sms received $smsStatus", Toast.LENGTH_SHORT).show()
                updateAppWidget("Sms received")
                fetchSMSLog()
               /* val phoneDataSubmitList : ArrayList<PhoneDataSubmitModel> = arrayListOf()
                if (smsLogArrayList.isNotEmpty()){
                    smsLogArrayList.forEach {
                        val phoneDataSubmitModel = PhoneDataSubmitModel(
                            direction = "sms",
                            duration = it.messageDate,
                            local_number = it.phoneNumber,
                            remote_number = it.contactName,
                            status = it.callType,
                            type = "sms"
                        )

                        phoneDataSubmitList.add(phoneDataSubmitModel)
                    }
                }
                val gson = Gson()
                val jsonData = gson.toJson(SubmitDataList(submitList = phoneDataSubmitList))

                val serviceIntent = Intent(context, BackgroundApiService::class.java).apply {
                    putExtra("data", jsonData)
                }
                context?.startService(serviceIntent)*/
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Call Logs"


        initUI()

    }

/*    override fun onPause() {
        super.onPause()`
     *//*   val packageName = applicationContext.packageName // Get your project's package name
        val mainActivityClassName = MainActivity::javaClass.name
        val componentName = ComponentName(packageName,mainActivityClassName)
        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP)*//*
    }*/

    private fun initUI() {

        val filter = IntentFilter("callStatus")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        val smsFilter = IntentFilter("smsStatus")
        LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, smsFilter)

        setupCallLogRecyclerView()
        setupSmsLogRecyclerView()
        setupHeader()

        if (checkPermissions(permissions = PERMISSIONS,this)){
            fetchCallLog()
            fetchSMSLog()
        }else{
            // Request the permissions from the user
            permissionResultLauncher.launch(PERMISSIONS)
        }

        binding.smsLogLayout.setOnClickListener {
            teleType = TeleTypeEnum.SMS
            setupHeader()
        }

        binding.callLogLayout.setOnClickListener {
            teleType = TeleTypeEnum.CALL
            setupHeader()
        }

    }

    private fun setupHeader() {

        when(teleType){
            TeleTypeEnum.CALL -> {
                binding.mainRv.visibility = android.view.View.VISIBLE
                binding.smsRv.visibility = android.view.View.GONE

                binding.smsLogLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_secondary_text))
                binding.callLogLayout.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_secondary20))


            }
            TeleTypeEnum.SMS -> {
                binding.mainRv.visibility = android.view.View.GONE
                binding.smsRv.visibility = android.view.View.VISIBLE

                binding.smsLogLayout.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_secondary20))
                binding.callLogLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_secondary_text))
            }
        }

    }

    private fun setupSmsLogRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.mainRv.layoutManager = layoutManager
        callLogAdapter = CallLogAdapter(callLogArrayList)
        binding.mainRv.adapter = callLogAdapter
    }

    private fun setupCallLogRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.smsRv.layoutManager = layoutManager
        smsLogAdapter = SmsLogAdapter(smsLogArrayList)
        binding.smsRv.adapter = smsLogAdapter
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

    private fun fetchSMSLog(){
        val sortOrder: String = Telephony.Sms.DATE + " DESC"

        val cursor: Cursor? = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            sortOrder
        )
        //clearing the arraylist
        smsLogArrayList.clear()

        if (cursor != null && cursor.moveToFirst()) {

            val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
            val messageIndex = cursor.getColumnIndex(Telephony.Sms.BODY)

            do {
                val callID = cursor.getString(idIndex)
                val phoneNumber = cursor.getString(numberIndex)
                val callDate = cursor.getString(dateIndex)
                val smsMessage = cursor.getString(messageIndex)
                var contactName = cursor.getString(numberIndex)
                contactName = if (contactName == null || contactName.equals("")) "Unknown" else contactName

                val dateFormatter = SimpleDateFormat(
                    "dd MMM yyyy", Locale.getDefault()
                )

                val str_call_date = dateFormatter.format(Date(callDate.toLong()))

                val timeFormatter = SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()
                )
                val str_call_time = timeFormatter.format(Date(callDate.toLong()))


                // Check if column index is -1, indicating the column doesn't exist
                if (idIndex != -1) {
                    // Process call history data here

                    val smsLogModel = MessageLogModel(
                        phoneNumber = phoneNumber,
                        messageDate = str_call_date,
                        message = smsMessage,
                        contactName = contactName
                    )
                    smsLogArrayList.add(smsLogModel)
                } else {
                    // Handle the case where the column doesn't exist

                }
            } while (cursor.moveToNext())
            cursor.close()

            smsLogAdapter.notifyDataSetChanged()
        }

    }

    private fun updateAppWidget(text: String) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, PhoneDirWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(packageName, R.layout.phone_dir_widget)
            views.setTextViewText(R.id.appwidget_text, text)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

}
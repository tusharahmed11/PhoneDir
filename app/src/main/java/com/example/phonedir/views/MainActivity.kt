package com.example.phonedir.views

import android.app.ActivityManager
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.CallLog
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phonedir.PhoneDirWidget
import com.example.phonedir.R
import com.example.phonedir.adapter.CallLogAdapter
import com.example.phonedir.adapter.SmsLogAdapter
import com.example.phonedir.data.TeleTypeEnum
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.data.model.MessageLogModel
import com.example.phonedir.data.model.PhoneDataSubmitModel
import com.example.phonedir.data.model.SubmitDataList
import com.example.phonedir.databinding.ActivityMainBinding
import com.example.phonedir.repository.MainRepository
import com.example.phonedir.service.AlwaysRunService
import com.example.phonedir.service.BServiceBinder
import com.example.phonedir.service.BackgroundApiService
import com.example.phonedir.utils.Utils
import com.example.phonedir.utils.Utils.checkPermissions
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Compiler.command
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var callLogArrayList: ArrayList<CallLogModel> = arrayListOf()
    private var smsLogArrayList: ArrayList<MessageLogModel> = arrayListOf()
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var smsLogAdapter: SmsLogAdapter
    private var teleType = TeleTypeEnum.CALL
    var isForeground = false

    @Inject
    lateinit var repository: MainRepository

    // Define a constant array of the permissions you need

    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) arrayOf(
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.FOREGROUND_SERVICE,
         android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
    ) else arrayOf(
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.FOREGROUND_SERVICE,
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

    private fun launchPer(){
        permissionResultLauncher.launch(PERMISSIONS)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "callStatus") {
                val callStatus = intent.getStringExtra("status")
                // Now you have the call status, do something with it
                when (callStatus) {
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                      //  Toast.makeText(context, "Phone call started", Toast.LENGTH_SHORT).show()

                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                     //   Toast.makeText(context, "Phone call ended", Toast.LENGTH_SHORT).show()
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
                        val isAppInBackground = ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED
                        val isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED


                        val gson = Gson()
                        val jsonData = gson.toJson(SubmitDataList(submitList = phoneDataSubmitList))
                    /*    val serviceIntent = Intent(context, BackgroundApiService::class.java).apply {
                            putExtra("data", jsonData)
                        }
                        context?.startService(serviceIntent)*/
                /*        if (!isAppInBackground ){
                            if (context != null)
                                ContextCompat.startForegroundService(context,serviceIntent);

                        }else{
                            context?.startService(serviceIntent)
                        }*/


                        /*val applicationContext = context!!.applicationContext
                        val intent = Intent(context, BackgroundApiService::class.java)
                        applicationContext.bindService(intent, object : ServiceConnection {
                            override fun onServiceConnected(name: ComponentName, binder: IBinder) {

                                if (binder is BServiceBinder) {
                                    val musicBinder: BServiceBinder = binder as BServiceBinder
                                    val service: BackgroundApiService? = musicBinder.service
                                    if (service != null) {
                                        // start a command such as music play or pause.
                                        service.startCommand(command)
                                        // force the service to run in foreground here.
                                        // the service is already initialized when bind and auto_create.
                                        service.startForegroundService()
                                    }
                                }
                                applicationContext.unbindService(this)
                            }

                            override fun onServiceDisconnected(name: ComponentName) {
                            }
                        }, BIND_AUTO_CREATE)*/
                        CoroutineScope(Dispatchers.IO).launch {

                            repository.getUserData().collectLatest { userDbList->
                                if (userDbList.isNotEmpty()){
                                    val token = "Bearer ${userDbList[0].accessToken}"
                                    try {
                                        val result = repository.submitApiCall(authToken = token, phoneDataSubmitModel = phoneDataSubmitList)
                                        Log.d("MyBackgroundService", "API call successful: $result")
                                    } catch (e: Exception) {
                                        Log.e("MyBackgroundService", "API call failed", e)
                                    }
                                }
                            }

                        }
                       /* val serviceIntent = Intent(context, BackgroundApiService::class.java).apply {
                            putExtra("data", jsonData)
                        }*/
                       /* if (Utils.isInBackground()){
                            context?.startService(serviceIntent)
                        }else{
                            context?.startForegroundService(serviceIntent)
                        }*/

                  //      context?.startService(serviceIntent)
                       /* if (context != null){
                            ContextCompat.startForegroundService(context,serviceIntent)
                        }*/
                       // context?.startService(serviceIntent)

                        /*context!!.bindService(serviceIntent, object : ServiceConnection {
                            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                                //retrieve an instance of the service here from the IBinder returned
                                //from the onBind method to communicate with
                            }

                            override fun onServiceDisconnected(name: ComponentName) {
                            }
                        }, BIND_AUTO_CREATE)*/
                     /*   if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                            context?.startForegroundService(serviceIntent)
                        }else{
                            context?.startService(serviceIntent)
                        }*/
                        /*val applicationContext = context!!.applicationContext;
                        applicationContext.bindService(serviceIntent, object : ServiceConnection{
                            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                                if (binder is BServiceBinder) {
                                    val musicBinder: BServiceBinder = binder as BServiceBinder
                                    val service: BackgroundApiService = musicBinder.getService()
                                    if (service != null) {
                                       *//* // start a command such as music play or pause.
                                        service.startCommand(command)
                                        // force the service to run in foreground here.
                                        // the service is already initialized when bind and auto_create.
                                        service.forceForeground()*//*
                                        service.startService(intent)
                                    }
                                }
                                applicationContext.unbindService(this)
                            }

                            override fun onServiceDisconnected(p0: ComponentName?) {
                                TODO("Not yet implemented")
                            }

                        }, BIND_AUTO_CREATE)*/

                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                      //  Toast.makeText(context, "Phone call ringing", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "smsStatus"){
                val smsStatus = intent.getStringExtra("status")
                //Toast.makeText(context, "Sms received $smsStatus", Toast.LENGTH_SHORT).show()
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

        val serviceIntent = Intent(this, AlwaysRunService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

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
    private val activityManager by lazy { getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }

    private fun isInForegroundByImportance(): Boolean {
        val importanceState = activityManager.runningAppProcesses.find {
            it.pid == android.os.Process.myPid()
        }?.importance ?: return false
        return importanceState >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    override fun onStop() {
        super.onStop()
        isForeground = true
    }

    override fun onResume() {
        super.onResume()
        isForeground = false
    }
}
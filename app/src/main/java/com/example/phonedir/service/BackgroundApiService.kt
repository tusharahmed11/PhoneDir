package com.example.phonedir.service

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.phonedir.data.model.SubmitDataList
import com.example.phonedir.repository.MainRepository
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class BackgroundApiService : Service(){
    @Inject
    lateinit var repository: MainRepository
    private val ANDROID_CHANNEL_ID: String = "com.xxxx.Location.Channel"


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
      /*  val NOTIFICATION_ID = (System.currentTimeMillis() % 10000).toInt()
        startForeground(NOTIFICATION_ID, Notification.Builder(this).build())*/
        val builder = Notification.Builder(this, ANDROID_CHANNEL_ID)
            .setContentTitle("phone dir")
            .setContentText("SmartTracker Running")
            .setAutoCancel(true)
        val notification = builder.build()
        startForeground(1234, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {




        intent?.let {
            val jsonData = it.getStringExtra("data")
            jsonData?.let {
                val gson = Gson()
                val data = gson.fromJson(jsonData, SubmitDataList::class.java)
                performApiCall(data)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun startForegroundService(service: Intent?): ComponentName? {

        service?.let {
            val jsonData = it.getStringExtra("data")
            jsonData?.let {
                val gson = Gson()
                val data = gson.fromJson(jsonData, SubmitDataList::class.java)
                performApiCall(data)
            }
        }
        return super.startForegroundService(service)
    }

    private fun performApiCall(data: SubmitDataList) {

/*
        CoroutineScope(Dispatchers.IO).launch {

            repository.getUserData().collectLatest { userDbList->
                if (userDbList.isNotEmpty()){
                    val token = "Bearer ${userDbList[0].accessToken}"
                    try {
                        val result = repository.submitApiCall(authToken = token, phoneDataSubmitModel = data.submitList)
                        Log.d("MyBackgroundService", "API call successful: $result")
                    } catch (e: Exception) {
                        Log.e("MyBackgroundService", "API call failed", e)
                    }
                }
            }

        }*/
    }



}
package com.example.phonedir.utils

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    companion object {

        lateinit var appContext: Context
        /*private const val WIFI_STATE_CHANGE_ACTION = "android.net.wifi.WIFI_STATE_CHANGED"


        fun getResourceString(resourceId: Int): String? {
            val appContext = appContext ?: return null
            val resources = appContext.resources
            return resources.getString(resourceId)
        }

        fun registerForNetworkChangeEvents(context: Context) {
            val networkStateChangeReceiver = NetworkStateChangeReceiver()
            context.registerReceiver(networkStateChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            context.registerReceiver(networkStateChangeReceiver, IntentFilter(WIFI_STATE_CHANGE_ACTION))
        }*/

    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
      /*  registerForNetworkChangeEvents(this)*/
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}
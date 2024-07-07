package com.example.phonedir

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class LifeApplication : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this);
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isAppInForeground = true
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isAppInForeground = false
    }

    companion object {
        var isAppInForeground = false
    }
}
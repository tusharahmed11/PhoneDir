package com.example.phonedir.service;

import android.os.Binder;

import java.lang.ref.WeakReference;

public class BServiceBinder  extends Binder {
    private WeakReference<BackgroundApiService> weakService;

    /**
     * Inject service instance to weak reference.
     */
    public void onBind(BackgroundApiService service) {
        this.weakService = new WeakReference<>(service);
    }

    public BackgroundApiService getService() {
        return weakService == null ? null : weakService.get();
    }
}

package com.example.chinhnb.placemap.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.chinhnb.placemap.Activity.LoginActivity;

/**
 * Created by CHINHNB on 12/30/2016.
 */

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            LoginActivity.startAt30(context);
        }
    }
}
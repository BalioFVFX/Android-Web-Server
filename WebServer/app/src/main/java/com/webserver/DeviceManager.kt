package com.webserver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings


class DeviceManager(private val context: Context) {

    private val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

    fun getBatteryLevel() : Float {
        val intent = context.registerReceiver(null, intentFilter)!!
        val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        return batteryLevel * 100 / scale.toFloat()
    }

    fun getDeviceName() : String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        } else {
            Build.MODEL
        }
    }
}
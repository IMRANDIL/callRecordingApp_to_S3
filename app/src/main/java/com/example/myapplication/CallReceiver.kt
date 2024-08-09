package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                context?.startService(Intent(context, CallRecordService::class.java))
            } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                context?.stopService(Intent(context, CallRecordService::class.java))
            }
        }
    }
}

package com.example.quittungsscanner.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.quittungsscanner.components.BroadcastActions.LOCAL_ACTION

class MyLocalBroadcastReciever: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == LOCAL_ACTION){
            Log.d("MyLocalBroadcastReceiver", "Broadcast recieved")
        } else {
            Log.d("MyLocalBroadcastReceiver","Unknown intent action: ${intent?.action}")
        }
    }
}
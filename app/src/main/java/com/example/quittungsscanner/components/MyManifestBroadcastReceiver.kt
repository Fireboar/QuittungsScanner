package com.example.quittungsscanner.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.quittungsscanner.components.BroadcastActions.MANIFEST_ACTION

class MyManifestBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == MANIFEST_ACTION){
            Log.d("MyManifestBroadcastReceiver", "Broadcast recieved")
        } else {
            Log.d("MyManifestBroadcastReceiver","Unknown intent action: ${intent?.action}")
        }
    }

}

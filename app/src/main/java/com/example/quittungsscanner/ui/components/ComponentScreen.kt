package com.example.quittungsscanner.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.example.quittungsscanner.components.BroadcastActions
import com.example.quittungsscanner.components.MusicPlayerService
import com.example.quittungsscanner.components.MyLocalBroadcastReciever
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun ComponentScreen(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ServiceWidget()
        BroadCastWidget()
        ContentProviderWidget()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContentProviderWidget() {
    Text(
        text = "Content Provider - SMS",
        style = MaterialTheme.typography.titleSmall
    )

    val permissionState = rememberPermissionState(
        android.Manifest.permission.READ_SMS
    )
    val isPermissionGranted = permissionState.status.isGranted
    if (!permissionState.status.isGranted){
        Button(
            onClick = {
                permissionState.launchPermissionRequest()
            }
        ) {
            Text("Request Permission")
        }
    }

    var smsList by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current
    if (isPermissionGranted){
        Button(
            onClick = {
                smsList = readSms(context)
            }
        ) {
            Text("Load SMS")
        }
        Column {
            smsList.forEach {sms ->
                Text(sms)
            }
        }
    }
    Spacer(modifier= Modifier.padding(10.dp))
}

private fun readSms(context: Context): List<String> {
    val smsList = mutableListOf<String>()
    context.contentResolver.query(
        Telephony.Sms.Inbox.CONTENT_URI,
        arrayOf(Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.BODY), // projection
        null, // selection
        null, // selection args
        null // sort order
    )?.let { cursor ->
        val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
        while (cursor.moveToNext()) {
            val body = cursor.getString(bodyIndex)
            smsList.add(body)
        }
        cursor.close()
    }
    return smsList
}

@Composable
fun BroadCastWidget() {
    val context = LocalContext.current
    LifecycleStartEffect(Unit) {
        val receiver = MyLocalBroadcastReciever()
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(BroadcastActions.LOCAL_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onStopOrDispose {
            context.unregisterReceiver(receiver)
        }
    }
    Text(
        text = "Broadcasts",
        style = MaterialTheme.typography.titleSmall
    )
    Button(
        onClick = {
            val intent = Intent(BroadcastActions.MANIFEST_ACTION)
            intent.`package` = "com.example.meineapp"
            context.sendBroadcast(intent)
        }
    ) {
        Text("Send Manifest Broadcast")
    }
    Button(
        onClick = {
            val intent = Intent(BroadcastActions.LOCAL_ACTION)
            intent.`package` = "com.example.meineapp"
            context.sendBroadcast(intent)
        }
    ) {
        Text("Send Local Broadcast")
    }
    Spacer(modifier= Modifier.padding(10.dp))
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ServiceWidget(){
    Text(
        text = "Service Widget",
        style = MaterialTheme.typography.titleSmall
    )
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        if (!permissionState.status.isGranted){
            Button(
                onClick = {
                    permissionState.launchPermissionRequest()
                }
            ) {
                Text("Request Permission")
            }
        }
    }
    Button(
        onClick = {
            val intent = Intent(context,MusicPlayerService::class.java)
            context.startService(intent)
        }
    ) {
        Text("Start service")
    }
    Button(
        onClick = {
            val intent = Intent(context,MusicPlayerService::class.java)
            context.stopService(intent)
        }
    ) {
        Text("Stop Service")
    }
    Spacer(modifier= Modifier.padding(10.dp))
}

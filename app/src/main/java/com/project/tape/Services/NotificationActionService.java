package com.project.tape.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationActionService extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("SONGS_SONGS")
                .putExtra("actionName", intent.getAction()));
    }
}

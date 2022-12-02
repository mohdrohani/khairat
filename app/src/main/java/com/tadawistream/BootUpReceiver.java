package com.tadawistream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class BootUpReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //System.out.println("Inside OnReceive");
        intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //context.startActivity(intent);
        //Toast.makeText(context, "Action: " + intent.getAction(), Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context.getApplicationContext()))
            {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                //startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        }
    }
}

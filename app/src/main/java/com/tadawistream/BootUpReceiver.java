package com.tadawistream;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class BootUpReceiver extends BroadcastReceiver
{
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent intentMain = new Intent(context, MainActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!Settings.canDrawOverlays(context.getApplicationContext()))
            {
                context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                //Toast.makeText(context.getApplicationContext(),"متصل",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(context.getApplicationContext(),"غير متصل",Toast.LENGTH_LONG).show();
            }

        }
    }
}

package com.tadawistream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class BootUpReceiver extends BroadcastReceiver
{
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //intent = new Intent(context, MainActivity.class);
        //intentMain.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //Toast.makeText(context,"Inside Receive",Toast.LENGTH_LONG).show();

        Intent intentMain = new Intent(context, MainActivity.class);
        //intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /*context.startActivity(intentMain);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context.getApplicationContext()))
            {
                context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                /*AlertDialog alertDialog0 = new AlertDialog.Builder(context).create();
                alertDialog0.setTitle("حالة أتصال الشاشه مع السيرفر");
                alertDialog0.setMessage("متصل");
                alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                alertDialog0.show();*/
                Toast.makeText(context.getApplicationContext(),"متصل",Toast.LENGTH_LONG).show();
            }
        }

        /*if(Intent.ACTION_BOOT_COMPLETED==intent.getAction())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context.getApplicationContext()))
                {
                    context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            }
        }
        if(Intent.ACTION_REBOOT==intent.getAction())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context.getApplicationContext()))
                {
                    context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            }
        }
        if(Intent.ACTION_SHUTDOWN==intent.getAction())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context.getApplicationContext()))
                {
                    context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            }
        }
        if(Intent.ACTION_LOCKED_BOOT_COMPLETED==intent.getAction())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context.getApplicationContext()))
                {
                    context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            }
        }*/
        /*if(ConnectivityManager.CONNECTIVITY_ACTION==intent.getAction())
        {
            AlertDialog alertDialog0 = new AlertDialog.Builder(context).create();
            alertDialog0.setTitle("حالة أتصال الشاشه مع السيرفر");
            alertDialog0.setMessage("لا يوجد أتصال مع السيرفر, يرجى التواصل بمدير تقنية المعلومات");
            alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            alertDialog0.show();
        }*/

        /*if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Toast.makeText(context,"Boot Completed",Toast.LENGTH_LONG).show();
        }
        if(Intent.ACTION_REBOOT.equals(intent.getAction()))
        {
            Toast.makeText(context,"Reboot Completed",Toast.LENGTH_LONG).show();
        }
        if(Intent.ACTION_SHUTDOWN.equals(intent.getAction()))
        {
            Toast.makeText(context,"Shutdown Completed",Toast.LENGTH_LONG).show();
        }
        if(Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Toast.makeText(context,"Locked Boot Completed",Toast.LENGTH_LONG).show();
        }
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
        {
            Toast.makeText(context,"Connectivity Changed",Toast.LENGTH_LONG).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context.getApplicationContext()))
            {
                context.startActivity(intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addCategory(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        }*/
    }
}

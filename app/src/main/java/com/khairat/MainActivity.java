package com.khairat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.net.Uri;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity
{
    BootUpReceiver receiver;
    IntentFilter intentFilter;
    //IntentFilter conFilter;
    public static final String runBeforePrefFile = "RunBeforeFile" ;
    public static final String serverIPPrefFile = "ServerIPFile" ;
    SharedPreferences.Editor editorServerIP;
    SharedPreferences preferencesServerIP;
    public static final String macAddressPrefFile = "MacAddressFile" ;
    SharedPreferences.Editor editorMacAddress;
    SharedPreferences preferencesMacAddress;
    private static final String PLAYBACK_TIME = "play_time";
    VideoView videoView;
    Uri uri;
    String mainResponse,urlPlay;
    MediaController mediaController;
    MediaPlayer mainMP;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.getIntent().setType("video/*");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            savedInstanceState.getInt(PLAYBACK_TIME);
        }

        try
        {
            receiver = new BootUpReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addCategory(getPackageName()+"android.intent.category.DEFAULT");
            intentFilter.addAction(getPackageName()+"android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_BOOT_COMPLETED");
            intentFilter.addAction(getPackageName()+"android.intent.action.REBOOT");
            intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_SHUTDOWN");
            intentFilter.addAction(getPackageName()+"android.intent.action.LOCKED_BOOT_COMPLETED");
            registerReceiver(receiver, intentFilter);

            if(isNetworkConnected())
            {
                if(isFirstTime())
                {
                    Intent intent = new Intent(this, init.class);
                    startActivityForResult(intent, 2);
                }
                else
                {
                    if((getServerIP() == null) || (getServerIP().equals("")))
                    {
                        Intent intent = new Intent(this, init.class);
                        startActivityForResult(intent, 2);
                    }
                    else
                    {
                        if(executeCommand(getServerIP()))
                        {
                            playVideo();
                        }
                    }
                }
            }
            else
            {
                Toast.makeText(MainActivity.this, "الشاشه ليست متصلة بشبكة واي فاي او سلكي " , Toast.LENGTH_LONG).show();
                //showMsg("وضع أتصال الشاشه","الشاشه ليست متصلة بشبكة واي فاي او سلكي");
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "الشاشه ليست متصلة بشبكة واي فاي او سلكي " + e.toString(), Toast.LENGTH_LONG).show();
            //showMsg("وضع أتصال الشاشه","الشاشه ليست متصلة بشبكة واي فاي او سلكي "+e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {        
        try
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 2)
            {                
                if (resultCode == RESULT_OK)
                {
                    setServerIP(data.getStringExtra("IP_Add"));
                    if(getTV_MacAddress()==null || getTV_MacAddress().equals(""))
                    {
                        setMac(randomMACAddress());
                        addDataToDatabase(getTvIP(),getMac(),getServerIP()); //Today
                    }
                    else
                    {
                        setMac(getTV_MacAddress());
                        addDataToDatabase(getTvIP(),getMac(),getServerIP()); //Today
                    }
                    if(executeCommand(getServerIP()))
                    {
                        playVideo();
                    }
                }
            }
        }
        catch(Exception e)
        {
            Toast.makeText(MainActivity.this, "يوجد خطأ في تشغيل الفيديو :" + e.toString(), Toast.LENGTH_LONG).show();
            //System.out.println("Inside On Activity Result Error: "+e.toString());
        }
    }

    public void playVideo()
    {
        urlPlay= "http://"+getServerIP()+"/getMedia.php";
        try
        {
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            StringRequest request = new StringRequest (Request.Method.POST,urlPlay,new com.android.volley.Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        mainResponse=response;
                        videoView = (VideoView) findViewById(R.id.videoView1);
                        uri = Uri.parse(response);
                        videoView.setVideoURI(uri);
                        mediaController = new MediaController(MainActivity.this);
                        mediaController.setAnchorView(videoView);
                        mediaController.setMediaPlayer(videoView);
                        videoView.requestFocus();
                        mediaController.setAnimationCacheEnabled(true);
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp)
                            {
                                mainMP=mp;
                                mainMP.start();
                            }
                        });
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp)
                            {
                                buildVideo();
                            }
                        });
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                //showMsg("يوجد خطأ في تشغيل الفيديو :","يوجد خطأ في تشغيل الفيديو");
                                Toast.makeText(MainActivity.this, "يوجد خطأ في تشغيل الفيديو :", Toast.LENGTH_LONG).show();
                                return false;
                            }
                        } );
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "يوجد خطأ في تشغيل الفيديو :" + e.toString(), Toast.LENGTH_LONG).show();
                        //showMsg("يوجد خطأ في تشغيل الفيديو :",e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Toast.makeText(MainActivity.this, "يوجد خطأ في تشغيل الفيديو :" + error.toString(), Toast.LENGTH_LONG).show();
                    //showMsg("يوجد خطأ في تشغيل الفيديو :",error.toString());
                }
            })
            {
                @Override
                public String getBodyContentType()
                {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tvMac", getMac());
                    return params;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "يوجد خطأ في تشغيل الفيديو :" + e.toString(), Toast.LENGTH_LONG).show();
            //showMsg("يوجد خطأ في تشغيل الفيديو :",e.toString());
        }
    }

    public void buildVideo()
    {
        try
        {
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            StringRequest request = new StringRequest (Request.Method.POST,urlPlay,new com.android.volley.Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        if(response==mainResponse)
                        {
                            mainMP.start();
                        }
                        else
                        {
                            mainResponse=response;
                            videoView = (VideoView) findViewById(R.id.videoView1);
                            uri = Uri.parse(response);
                            videoView.setVideoURI(uri);
                            mediaController = new MediaController(MainActivity.this);
                            mediaController.setAnchorView(videoView);
                            mediaController.setMediaPlayer(videoView);
                            videoView.requestFocus();
                            mediaController.setAnimationCacheEnabled(true);

                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp)
                                {
                                    mainMP=mp;
                                    mainMP.start();
                                }
                            });
                            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp)
                                {
                                    playVideo();
                                }
                            });
                            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Toast.makeText(MainActivity.this,"يوجد خطأ في أعادة تشغيل الفيديو", Toast.LENGTH_LONG).show();
                                    //showMsg("يوجد خطأ في أعادة تشغيل الفيديو :","يوجد خطأ في أعادة تشغيل الفيديو");
                                    return false;
                                }
                            } );
                        }

                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "يوجد خطأ في أعادة تشغيل الفيديو :" + e.toString(), Toast.LENGTH_LONG).show();
                        //showMsg("يوجد خطأ في أعادة تشغيل الفيديو :",e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //showMsg("PlayVideo Fail to get response",error.toString());
                    Toast.makeText(MainActivity.this, "Fail to get response = " + error.toString(), Toast.LENGTH_SHORT).show();
                }
            })
            {
                @Override
                public String getBodyContentType()
                {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tvMac", getMac());
                    return params;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this,"يوجد خطأ في أعادة تشغيل الفيديو", Toast.LENGTH_LONG).show();
            //showMsg("Msg7:",e.toString());
        }
    }

    private String randomMACAddress()
    {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){

            if(sb.length() > 0)
                sb.append(":");

            sb.append(String.format("%02x", b));
        }


        return sb.toString();
    }

    public String getTvIP()
    {
        String sAddr="";
        if(isWifiConnected())
        {
            try
            {
                List<NetworkInterface> networkInterfaceList= Collections.list(NetworkInterface.getNetworkInterfaces());
                for(NetworkInterface networkInterface:networkInterfaceList)
                {
                    List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
                    for (InetAddress addr : addrs)
                    {
                        if (!addr.isLoopbackAddress())
                        {
                            sAddr = addr.getHostAddress();
                        }
                    }
                    break;
                }
            }
            catch(Exception e)
            {
                Toast.makeText(this, " خطأ الأيبي أدرس "+e.toString(), Toast.LENGTH_LONG).show();
                //showMsg("خطأ الأيبي أدرس",e.toString());
            }                        
        }
        if(isEthernetConnected())
        {
            try
            {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface myinterface : interfaces)
                {
                    List<InetAddress> addrs = Collections.list(myinterface.getInetAddresses());
                    for (InetAddress addr : addrs)
                    {
                        if (!addr.isLoopbackAddress() && addr instanceof Inet4Address)
                        {
                            sAddr = addr.getHostAddress();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                //showMsg("خطأ الأيبي أدرس",e.toString());
                Toast.makeText(this, " خطأ الأيبي أدرس "+e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return sAddr;
    }

    public String getTV_MacAddress()
    {
        String stringMac="";
        if(isWifiConnected())
        {
            try
            {
                List<NetworkInterface> networkInterfaceList= Collections.list(NetworkInterface.getNetworkInterfaces());
                for(NetworkInterface networkInterface:networkInterfaceList)
                {
                    for (int i=0; i<networkInterface.getHardwareAddress().length;i++)
                    {
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i] & 0xFF);
                        if (stringMacByte.length() == 1)
                        {
                            stringMacByte = "0" + stringMacByte;
                        }
                        if(networkInterface.getHardwareAddress().length==i+1) {
                            stringMac =stringMac + stringMacByte.toUpperCase();
                        }
                        else
                        {
                            stringMac =stringMac + stringMacByte.toUpperCase() + ":";
                        }
                    }
                    break;
                }
            }
            catch(Exception e)
            {
                //showMsg("خطأ الماك أدرس",e.toString());
                Toast.makeText(this, "Wifi Mac address Error "+e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        if(isEthernetConnected())
        {
            try
            {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface myinterface : interfaces)
                {
                    for (int i=0; i<myinterface.getHardwareAddress().length;i++)
                    {
                        String stringMacByte = Integer.toHexString(myinterface.getHardwareAddress()[i] & 0xFF);
                        if (stringMacByte.length() == 1)
                        {
                            stringMacByte = "0" + stringMacByte;
                        }
                        if(myinterface.getHardwareAddress().length==i+1) {
                            stringMac =stringMac + stringMacByte.toUpperCase();
                        }
                        else
                        {
                            stringMac =stringMac + stringMacByte.toUpperCase() + ":";
                        }
                    }
                }
            }
            catch(Exception e)
            {
                //showMsg("خطأ الماك أدرس",e.toString());
                Toast.makeText(this, "Mac address Error "+e.toString(), Toast.LENGTH_LONG).show();
            }

        }
        return stringMac;
    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public Boolean isWifiConnected()
    {
        Boolean res;
        ConnectivityManager cm;
        if(isNetworkConnected()){
            cm= (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            res=(cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
        }
        else
        {
            res=false;
        }
        return res;
    }

    public Boolean isEthernetConnected()
    {
        Boolean res;
        ConnectivityManager cm;
        if(isNetworkConnected()){
            cm= (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            res=(cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        }
        else
        {
            res=false;
        }
        return res;
    }

    private void setServerIP(String ipaddress)
    {
        preferencesServerIP=getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);
        editorServerIP = preferencesServerIP.edit();
        editorServerIP.putString("serverIP", ipaddress);
        editorServerIP.commit();
    }

    private String getServerIP()
    {
        preferencesServerIP= getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);
        return preferencesServerIP.getString("serverIP",null);
    }

    private void setMac(String macAddress)
    {
        preferencesMacAddress=getSharedPreferences(macAddressPrefFile, this.MODE_PRIVATE);
        editorMacAddress = preferencesMacAddress.edit();
        editorMacAddress.putString("ethernetMacAddr", macAddress);
        editorMacAddress.commit();
    }

    private String getMac()
    {
        preferencesMacAddress= getSharedPreferences(macAddressPrefFile, this.MODE_PRIVATE);
        return preferencesMacAddress.getString("ethernetMacAddr",null);
        
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferencesRunBefore= getSharedPreferences(runBeforePrefFile, this.MODE_PRIVATE);
        boolean ranBefore = preferencesRunBefore.contains("runBefore");
        if (!ranBefore)
        {
            SharedPreferences.Editor editorRunBefore = preferencesRunBefore.edit();
            editorRunBefore.putBoolean("runBefore", true);
            editorRunBefore.commit();
            return true;
        }
        else
        {
            //System.out.println("Is First Time "+false);
            //System.out.println("Server IP="+getServerIP());
            return false;
        }
    }

    private boolean executeCommand(String ip)
    {
        boolean myReturn=false;
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpProcess = runtime.exec("/system/bin/ping -c 1 "+ip);
            int mExitValue = mIpProcess.waitFor();

            if(mExitValue==0){
                myReturn=true;
            }else{
                myReturn=false;
                //showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر"+"\\n"+ip);
                Toast.makeText(this, "لا يوجد أتصال مع السيرفر", Toast.LENGTH_LONG).show();
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            //showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر"+ignore.toString());
            Toast.makeText(this, "لا يوجد أتصال مع السيرفر", Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            //showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر "+e.toString());
            Toast.makeText(this, "لا يوجد أتصال مع السيرفر", Toast.LENGTH_LONG).show();
        }
        return myReturn;
    }

    private void addDataToDatabase(String tvIP, String tvMac, String serverIP)
    {
        String url = "http://"+serverIP+"/php/addTVs.php";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest (Request.Method.POST, url, new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            if(Boolean.parseBoolean(response))
                            {
                                Toast.makeText(MainActivity.this, "تم أضافة هذا التلفزيون بنجاح " + response, Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                AlertDialog alertDialog0 = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog0.setTitle("خطأ: لم يتم أضافة هذا التلفزيون بنجاح ");
                                alertDialog0.setMessage("يرجى الأتصال على 0551129620");
                                alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        System.exit(0);
                                    }
                                });
                                alertDialog0.show();
                            }
                        }
                        catch (Exception e)
                        {
                            showMsg("خطأ: لم يتم أضافة هذا التلفزيون بنجاح ","يرجى الأتصال بي 0551129620");
                        }
                    }
                },new com.android.volley.Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(MainActivity.this,"خطأ في أضافة التلفزيون الي قاعدة البيانات: "+error.toString(),Toast.LENGTH_LONG).show();
                        //showMsg("خطأ في أضافة التلفزيون الي قاعدة البيانات",error.toString());
                    }
                })
                {
                    @Override
                    public String getBodyContentType()
                    {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("tvIP", tvIP);
                        params.put("tvMac", tvMac);
                        params.put("serverIP", serverIP);
                        return params;
                    }
                };
        queue.add(request);
    }

    public void showMsg(String title, String msg)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run()
            {
                AlertDialog alertDialog0 = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog0.setTitle(title);
                alertDialog0.setMessage(msg);
                alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                alertDialog0.show();
                // write your code here
            }
        });

    }
}




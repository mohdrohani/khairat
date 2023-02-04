package com.tadawistream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.net.Uri;
import android.app.Activity;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class MainActivity extends Activity
{
    BootUpReceiver receiver;
    IntentFilter intentFilter;
    //IntentFilter conFilter;
    public static final String runBeforePrefFile = "RunBeforeFile" ;
    public static final String serverIPPrefFile = "ServerIPFile" ;
    SharedPreferences.Editor editorServerIP;
    SharedPreferences preferencesServerIP;

    private static final String PLAYBACK_TIME = "play_time";
    VideoView videoView;
    Uri uri;
    //String mainResponse;
    String urlPlay;
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
        //System.out.println("inside OnCreate");

        try
        {
            receiver = new BootUpReceiver();
            intentFilter = new IntentFilter();
            //conFilter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addCategory(getPackageName()+"android.intent.category.DEFAULT");
            intentFilter.addAction(getPackageName()+"android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_BOOT_COMPLETED");
            intentFilter.addAction(getPackageName()+"android.intent.action.REBOOT");
            intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_SHUTDOWN");
            intentFilter.addAction(getPackageName()+"android.intent.action.LOCKED_BOOT_COMPLETED");
            //registerReceiver(receiver, conFilter);
            registerReceiver(receiver, intentFilter);

            if(isNetworkConnected())
            {
                //Toast.makeText(this, "is First Time:"+isFirstTime(), Toast.LENGTH_LONG).show();
                //System.out.println("is First Time:"+isFirstTime());
                if(isFirstTime())
                {
                    Intent intent = new Intent(this, init.class);
                    startActivityForResult(intent, 2);
                }
                else
                {
                    //Toast.makeText(this, "is Not First Time:"+isFirstTime(), Toast.LENGTH_LONG).show();
                    //System.out.println("It is not First Time");
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
                AlertDialog alertDialog0 = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog0.setTitle("وضع أتصال الشاشه");
                alertDialog0.setMessage("الشاشه ليست متصلة بشبكة واي فاي او سلكي");
                alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                alertDialog0.show();
            }
        }
        catch (Exception e)
        {
            System.out.println("Error On Create:"+e.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //System.out.println("inside OnDestroy");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("inside OnDestroy");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //System.out.println("inside OnDestroy");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //System.out.println("inside OnDestroy");
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
                    addDataToDatabase(getTvIP(),getTV_MacAddress(),getServerIP()); //Today
                    //getMedia(getTV_MacAddress(),getServerIP());
                    if(executeCommand(getServerIP()))
                    {
                        playVideo();
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Inside On Activity Result Error: "+e.toString());
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
                        //mainResponse=response;
                        //uri = Uri.parse(mainResponse);
                        uri = Uri.parse(response);
                        videoView = (VideoView) findViewById(R.id.videoView1);

                        videoView.setVideoURI(uri);
                        videoView.requestFocus();

                        mediaController = new MediaController(MainActivity.this);
                        mediaController.setAnchorView(videoView);
                        mediaController.setAnimationCacheEnabled(true);


                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp)
                            {
                                mainMP=mp;
                                mp.setLooping(true);
                                mp.start();
                            }
                        });
                        //System.out.println("video url :"+response);
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp)
                            {
                                //buildVideo();
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        System.out.println("يوجد خطأ في تشغيل الفيديو :" + e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    System.out.println("يوجد خطأ في تشغيل الفيديو :" + error.toString());
                    //Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
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
                    params.put("tvMac", getTV_MacAddress());
                    return params;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            //Toast.makeText(this, "Error connecting", Toast.LENGTH_LONG).show();
            System.out.println("Play Video Error connecting"+e.toString());
        }
    }

    /*public void buildVideo()
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
                            //videoView = (VideoView) findViewById(R.id.videoView1);
                            uri = Uri.parse(mainResponse);
                            videoView.setVideoURI(uri);
                            //MediaController mediaController = new MediaController(MainActivity.this);
                            //mediaController.setAnchorView(videoView);
                            //mediaController.setAnimationCacheEnabled(true);
                            //videoView.requestFocus();
                            videoView.setOnPreparedListener(new OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp)
                                {
                                    mp.start();
                                }
                            });
                            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp)
                                {
                                    playVideo();
                                }
                            });
                        }

                    }
                    catch (Exception e)
                    {
                        System.out.println("يوجد خطأ في أعادة تشغيل الفيديو :" + e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    System.out.println("PlayVideo Fail to get response = " + error.toString());
                    //Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
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
                    params.put("tvMac", getTV_MacAddress());
                    return params;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            //Toast.makeText(this, "Error connecting", Toast.LENGTH_LONG).show();
            System.out.println("Play Video Error connecting"+e.toString());
        }
    }*/

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
                //Toast.makeText(this, "IP address Error "+e.toString(), Toast.LENGTH_LONG).show();
                System.out.println("getIP Error connecting :"+e.toString());

            }
        }
        if(isEthernetConnected())
        {
            try
            {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces)
                {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
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
                //Toast.makeText(this, "IP address Error "+e.toString(), Toast.LENGTH_LONG).show();
                System.out.println("getIP Error connecting :"+e.toString());

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
                        //System.out.println(stringMacByte.length());
                        if (stringMacByte.length() == 1)
                        {
                            //System.out.println("->"+"0" + stringMacByte);
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
                //Toast.makeText(this, "Mac address Error "+e.toString(), Toast.LENGTH_LONG).show();
                System.out.println("Mac address Error :"+e.toString());
            }
        }
        if(isEthernetConnected())
        {
            try
            {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                //System.out.println("hello");
                for (NetworkInterface intf : interfaces)
                {
                    for (int i=0; i<intf.getHardwareAddress().length;i++)
                    {
                        String stringMacByte = Integer.toHexString(intf.getHardwareAddress()[i] & 0xFF);
                        //System.out.println(stringMacByte.length());
                        if (stringMacByte.length() == 1)
                        {
                            //System.out.println("->"+"0" + stringMacByte);
                            stringMacByte = "0" + stringMacByte;
                        }
                        if(intf.getHardwareAddress().length==i+1) {
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
                //Toast.makeText(this, "Mac address Error "+e.toString(), Toast.LENGTH_LONG).show();
                System.out.println("Mac address Error :"+e.toString());
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
        if(isNetworkConnected()){
            ConnectivityManager cm
                    = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
        }
        return false;
    }

    public Boolean isEthernetConnected()
    {
        if(isNetworkConnected()){
            ConnectivityManager cm
                    = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        }
        return false;
    }

    private void setServerIP(String ipaddress)
    {
        //Toast.makeText(this, "IP address :"+ipaddress, Toast.LENGTH_LONG).show();
        //System.out.println("IP address :"+ipaddress);
        preferencesServerIP=getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);
        editorServerIP = preferencesServerIP.edit();
        editorServerIP.putString("serverIP", ipaddress);
        editorServerIP.commit();
    }

    private String getServerIP()
    {
        preferencesServerIP= getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);
        String servIP = preferencesServerIP.getString("serverIP",null);
        return servIP;
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferencesRunBefore= getSharedPreferences(runBeforePrefFile, this.MODE_PRIVATE);
        boolean ranBefore = preferencesRunBefore.contains("runBefore");
        if (!ranBefore)
        {
            //System.out.println("Is First Time "+true);
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
            //System.out.println(" mExitValue "+mExitValue);
            //Toast.makeText(this, " mExitValue "+mExitValue, Toast.LENGTH_LONG).show();

            if(mExitValue==0){
                myReturn=true;
            }else{
                myReturn=false;
                AlertDialog alertDialog0 = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog0.setTitle("حالة أتصال الشاشه مع السيرفر");
                alertDialog0.setMessage("لا يوجد أتصال مع السيرفر, يرجى التواصل بمدير تقنية المعلومات");
                alertDialog0.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                });
                alertDialog0.show();
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            //System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //System.out.println(" Exception:"+e);
        }
        return myReturn;
    }

    private void addDataToDatabase(String tvIP, String tvMac, String serverIP)
    {
        String url = "http://"+serverIP+"/addTVs.php";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new
                StringRequest
                (Request.Method.POST, url, new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            System.out.println("Data inserted to database:" + response);
                        }
                        catch (Exception e)
                        {
                            System.out.println("AddDataToDatabase Error :" + e.toString());
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        System.out.println("Fail to insert data to database = " + error.toString());
                    }
                }
                )
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
                        //System.out.println("AddDataToDatabase TvIP :"+params.get("tvIP"));
                        //System.out.println("AddDataToDatabase TvMac :"+params.get("tvMac"));
                        return params;
                    }
                };
        queue.add(request);
    }
}




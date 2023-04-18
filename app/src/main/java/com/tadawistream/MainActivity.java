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

import android.content.Context;

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
        //System.out.println("inside OnCreate");

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
                    //System.out.println("It is not First Time");
                    if((getServerIP() == null) || (getServerIP().equals("")))
                    {
                        Intent intent = new Intent(this, init.class);
                        startActivityForResult(intent, 2);
                    }
                    else
                    {
                        //showMsg("ServerIP",getServerIP());
                        //showMsg("TV information:","\n\r MAC :"+getMac());
                        if(executeCommand(getServerIP()))
                        {
                            playVideo();
                        }
                    }
                }
            }
            else
            {
                showMsg("وضع أتصال الشاشه","الشاشه ليست متصلة بشبكة واي فاي او سلكي");
            }
        }
        catch (Exception e)
        {
            //System.out.println("Error On Create:"+e.toString());
            showMsg("وضع أتصال الشاشه","الشاشه ليست متصلة بشبكة واي فاي او سلكي "+e.toString());
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

                    if(getTV_MacAddress()==null || getTV_MacAddress().equals(""))
                    {
                        setMac(randomMACAddress());
                        //showMsg("TV information:","\n\r MAC :"+getMac());
                        addDataToDatabase(getTvIP(),getMac(),getServerIP()); //Today
                    }
                    else
                    {
                        setMac(getTV_MacAddress());
                        //showMsg("TV information:","\n\r MAC :"+getMac());
                        addDataToDatabase(getTvIP(),getMac(),getServerIP()); //Today
                    }

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
                        mainResponse=response;
                        //uri = Uri.parse(mainResponse);
                        //Toast.makeText(MainActivity.this, "Msg1=>"+response.getBytes().toString(), Toast.LENGTH_LONG).show();
                        //System.out.println("video url :"+response);
                        //showMsg("Msg1:",response.substring(0,1000));
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
                                //mp.setLooping(true);
                                mainMP.start();
                                //showMsg("Msg1:",mainResponse.toString());
                            }
                        });
                        //System.out.println("video url :"+response);
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
                                showMsg("يوجد خطأ في تشغيل الفيديو :","يوجد خطأ في تشغيل الفيديو");
                                return false;
                            }
                        } );
                    }
                    catch (Exception e)
                    {
                        //System.out.println("يوجد خطأ في تشغيل الفيديو :" + e.toString());
                        //Toast.makeText(MainActivity.this, "Msg2=>" + e.toString(), Toast.LENGTH_LONG).show();
                        showMsg("يوجد خطأ في تشغيل الفيديو :",e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //System.out.println("يوجد خطأ في تشغيل الفيديو :" + error.toString());
                    //Toast.makeText(MainActivity.this, "Msg3=>" + error.toString(), Toast.LENGTH_LONG).show();
                    showMsg("يوجد خطأ في تشغيل الفيديو :",error.toString());
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
            // TODO: handle exception
            //Toast.makeText(MainActivity.this, "Msg4=>" + e.toString(), Toast.LENGTH_LONG).show();
            showMsg("يوجد خطأ في تشغيل الفيديو :",e.toString());
            //System.out.println("Play Video Error connecting"+e.toString());
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
                            //videoView = (VideoView) findViewById(R.id.videoView1);
                            //Toast.makeText(MainActivity.this, "Msg9=>" + mainResponse.toString(), Toast.LENGTH_LONG).show();
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
                                    showMsg("يوجد خطأ في أعادة تشغيل الفيديو :","يوجد خطأ في أعادة تشغيل الفيديو");
                                    return false;
                                }
                            } );
                        }

                    }
                    catch (Exception e)
                    {
                        //Toast.makeText(MainActivity.this, "Msg5=>" + e.toString(), Toast.LENGTH_LONG).show();
                        showMsg("يوجد خطأ في أعادة تشغيل الفيديو :",e.toString());
                        //System.out.println("يوجد خطأ في أعادة تشغيل الفيديو :" + e.toString());
                    }
                }
            }, new com.android.volley.Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //Toast.makeText(MainActivity.this, "Msg6=>" + error.toString(), Toast.LENGTH_LONG).show();
                    showMsg("Msg6:",error.toString());
                    //System.out.println("PlayVideo Fail to get response = " + error.toString());
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
                    params.put("tvMac", getMac());
                    return params;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            //Toast.makeText(MainActivity.this, "Msg7=>" + e.toString(), Toast.LENGTH_LONG).show();
            showMsg("Msg7:",e.toString());
            //System.out.println("Play Video Error connecting"+e.toString());
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
                //Toast.makeText(this, "IP address Error "+e.toString(), Toast.LENGTH_LONG).show();
                //showMsg("حطأ الأيبي أدرس",e.toString());
                System.out.println("getIP Error connecting :"+e.toString());

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
                //showMsg("حطأ الأيبي أدرس",e.toString());
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
                //showMsg("حطأ الماك أدرس",e.toString());
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
                for (NetworkInterface myinterface : interfaces)
                {
                    for (int i=0; i<myinterface.getHardwareAddress().length;i++)
                    {
                        String stringMacByte = Integer.toHexString(myinterface.getHardwareAddress()[i] & 0xFF);
                        //System.out.println(stringMacByte.length());
                        if (stringMacByte.length() == 1)
                        {
                            //System.out.println("->"+"0" + stringMacByte);
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
                //showMsg("حطأ الماك أدرس",e.toString());
                //Toast.makeText(this, "Mac address Error "+e.toString(), Toast.LENGTH_LONG).show();
                System.out.println("Mac address Error :"+e.toString());
            }

        }
        //Toast.makeText(MainActivity.this, "Msg8=>" + stringMac.toString(), Toast.LENGTH_LONG).show();
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
        return preferencesServerIP.getString("serverIP",null);
    }

    private void setMac(String macAddress)
    {
        //Toast.makeText(this, "IP address :"+ipaddress, Toast.LENGTH_LONG).show();
        //System.out.println("IP address :"+ipaddress);
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
                showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر");
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            //Toast.makeText(this, "داخل ايكزيكيوت: "+ignore, Toast.LENGTH_LONG).show();
            showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر"+ignore.toString());
            //System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            //Toast.makeText(this, "داخل ايكزيكيوت: "+ e, Toast.LENGTH_LONG).show();
            showMsg("حالة أتصال الشاشه مع السيرفر","لا يوجد أتصال مع السيرفر "+e.toString());
            //System.out.println(" Exception:"+e);
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
                            //Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                            System.out.println("Data inserted to database:" + response);
                        }
                        catch (Exception e)
                        {
                            showMsg("حطأ في قاعدة البيانات",e.toString());
                            //Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                            //System.out.println("AddDataToDatabase Error :" + e.toString());
                        }
                    }
                },new com.android.volley.Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        showMsg("حطأ في قاعدة البيانات",error.toString());
                        //System.out.println("Fail to insert data to database = " + error.toString());
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
                        //System.out.println("AddDataToDatabase TvIP :"+params.get("tvIP"));
                        //System.out.println("AddDataToDatabase TvMac :"+params.get("tvMac"));
                        return params;
                    }
                };
        queue.add(request);
    }
    public void showMsg(String title, String msg)
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
    }
}




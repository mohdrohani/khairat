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
    public static final String runBeforePrefFile = "RunBeforeFile" ;
    public static final String serverIPPrefFile = "ServerIPFile" ;
    SharedPreferences.Editor editorServerIP;
    SharedPreferences preferencesServerIP;
    private int mCurrentPosition;
    private static final String PLAYBACK_TIME = "play_time";
    VideoView videoView;
    Uri uri;
    String mainResponse;
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
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }
        //System.out.println("inside OnCreate");
        receiver = new BootUpReceiver();
        intentFilter = new IntentFilter();
        try
        {
            if(isNetworkConnected())
            {
                //Toast.makeText(this, "is First Time:"+isFirstTime(), Toast.LENGTH_LONG).show();
                //System.out.println("Network Connected");
                if(!isFirstTime())
                {
                    //Toast.makeText(this, "is First Time:"+isFirstTime(), Toast.LENGTH_LONG).show();
                    //System.out.println("It is First Time");
                    Intent intent = new Intent(this, init.class);
                    startActivityForResult(intent, 2);
                }
                else
                {
                    //Toast.makeText(this, "is Not First Time:"+isFirstTime(), Toast.LENGTH_LONG).show();
                    //System.out.println("It is not First Time");
                    if((getServerIP() == null) || (getServerIP() == ""))
                    {
                        if(serverReachable(getServerIP()))
                        {
                            Intent intent = new Intent(this, init.class);
                            startActivityForResult(intent, 2);
                            //System.out.println("Server IP if Null:"+getServerIP());
                        }
                        else
                        {
                            //Toast.makeText(this, "Cannot Access Server", Toast.LENGTH_LONG).show();
                            System.out.println("Server not reachable");
                        }

                    }
                    else
                    {
                        if(serverReachable(getServerIP()))
                        {
                            playVideo();
                        }
                        else
                        {
                            //Toast.makeText(this, "Cannot Access Server", Toast.LENGTH_LONG).show();
                            System.out.println("Server not reachable");
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
    protected void onStart()
    {
        super.onStart();
        System.out.println("inside OnStart");
        intentFilter.addCategory(getPackageName()+"android.intent.category.DEFAULT");
        intentFilter.addAction(getPackageName()+"android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_BOOT_COMPLETED");
        intentFilter.addAction(getPackageName()+"android.intent.action.REBOOT");
        intentFilter.addAction(getPackageName()+"android.intent.action.QUICKBOOT_POWERON");
        intentFilter.addAction(getPackageName()+"android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction(getPackageName()+"android.intent.action.LOCKED_BOOT_COMPLETED");

        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("inside OnStop");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("inside OnResume");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("inside OnDestroy");
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
                    addDataToDatabase(getTV_IPaddress(),getTV_MacAddress(),getServerIP()); //Today
                    //getMedia(getTV_MacAddress(),getServerIP());
                    if(serverReachable(getServerIP()))
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
        //System.out.println("Inside PlayVideo");
        urlPlay= "http://"+getServerIP()+"/getMedia.php";
        System.out.println("Inside PlayVideo URL :"+urlPlay);
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
                        System.out.println("video url :"+response);
                        videoView = (VideoView) findViewById(R.id.videoView1);
                        uri = Uri.parse(mainResponse);
                        videoView.setVideoURI(uri);
                        System.out.println("video uri :"+uri.toString());
                        mediaController = new MediaController(MainActivity.this);
                        mediaController.setAnchorView(videoView);
                        mediaController.setAnimationCacheEnabled(true);
                        videoView.requestFocus();
                        videoView.setOnPreparedListener(new OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp)
                            {
                                mainMP=mp;
                                mp.start();
                            }
                        });
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp)
                            {
                                reconfigeVideo();
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        System.out.println("JSon Error :" + e.toString());
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
    }

    public void reconfigeVideo()
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
                        System.out.println("JSon Error :" + e.toString());
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
    }

    public String getTV_IPaddress()
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
        System.out.println("IP address :"+ipaddress);
        preferencesServerIP=getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);
        editorServerIP = preferencesServerIP.edit();
        editorServerIP.putString("serverIP", ipaddress);
        editorServerIP.commit();
    }

    private String getServerIP()
    {
        preferencesServerIP= getSharedPreferences(serverIPPrefFile, this.MODE_PRIVATE);;
        String servIP = preferencesServerIP.getString("serverIP",null);
        return servIP;
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferencesRunBefore= getSharedPreferences(runBeforePrefFile, this.MODE_PRIVATE);;
        boolean ranBefore = preferencesRunBefore.contains("runBefore");
        //System.out.println("Ran Before="+ranBefore);
        //System.out.println("Server IP="+getServerIP());
        //System.out.println("equal to blank:"+(getServerIP()==""));
        //System.out.println("equal to null:"+(getServerIP()==null));

        if (!ranBefore)
        {
            SharedPreferences.Editor editorRunBefore = preferencesRunBefore.edit();
            editorRunBefore.putBoolean("runBefore", true);
            editorRunBefore.commit();
        }
        else
        {
            System.out.println("Ran Before="+ranBefore);
            System.out.println("Server IP="+getServerIP());
        }
        return ranBefore;
    }

    public static boolean serverReachable(String ip) throws IOException
    {
        boolean reachable=false;
        //System.out.println("inside function serverReachable");
        ProcessBuilder pb = new ProcessBuilder("ping", ip);
        //ProcessBuilder pb = new ProcessBuilder("ping", "-c 5", ip);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.start().getInputStream()));
        String line;
        //ArrayList<String> output = new ArrayList<>();

        if ((line = stdInput.readLine()) != null)
        {
            System.out.println("Line="+line);
            reachable=true;
        }
        //System.out.println("after if");
        //System.out.println("Reachable="+reachable);
        return reachable;
    }

    private void addDataToDatabase(String tvIP, String tvMac, String serverIP)
    {
        //String url = "http://"+getServerIP()+"/addTVs.php";
        String url = "http://"+serverIP+"/addTVs.php";
        //Toast.makeText(this, "URL addTVs="+url, Toast.LENGTH_LONG).show();
        System.out.println("URL addTVs :"+url);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    System.out.println("Response :" + response);
                }
                catch (Exception e)
                {
                    System.out.println("AddDataToDatabase Error :" + e.toString());
                }
            }
        }, new com.android.volley.Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                System.out.println("Fail to get response = " + error.toString());
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
                System.out.println("AddDataToDatabase TvIP :"+params.get("tvIP"));
                System.out.println("AddDataToDatabase TvMac :"+params.get("tvMac"));
                return params;
            }
        };
        queue.add(request);
    }

    /*public void getMedia(String tvMac, String serverIP)
    {
        String url = "http://"+serverIP+"/getMedia.php";
        Toast.makeText(this, "URL="+url, Toast.LENGTH_LONG).show();
        //final String[] mediaUrl = new String[1];

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        StringRequest request = new StringRequest (Request.Method.POST,url,new com.android.volley.Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    setMediaUrl(response);
                    //mStr[0]=response;
                    System.out.println("mediaStr=" + response.toString());
                    //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();

                }
                catch (Exception e)
                {
                    System.out.println("JSon Error :" + e.toString());
                }
                // and setting data to edit text as empty

            }
        }, new com.android.volley.Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                // method to handle errors.
                System.out.println("Fail to get response = " + error.toString());
                //Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            public String getBodyContentType()
            {
                // as we are passing data in the form of url encoded
                // so we are passing the content type below
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {

                // below line we are creating a map for storing
                // our values in key and value pair.
                Map<String, String> params = new HashMap<String, String>();

                // on below line we are passing our
                // key and value pair to our parameters.
                params.put("tvMac", tvMac);

                //System.out.println("Params 1"+params.get("tvMac"));
                // at last we are returning our params.
                return params;
            }
        };
        // below line is to make
        // a json object request.
        queue.add(request);
    }*/

    /*public void setMediaUrl(String resp)
    {
        mediaLink=resp;
        //System.out.println("Inside setMedia:"+mediaLink);
    }
    public String getMediaUrl()
    {
        //System.out.println("Inside getMedia:"+mediaLink);
        return mediaLink;
    }*/

}




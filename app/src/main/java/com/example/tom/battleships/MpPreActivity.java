package com.example.tom.battleships;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MpPreActivity extends MainMenuActivity {

    Handler uiHandler;
    Button btnHost, btnClient;
    EditText serverIPText;
    public static String SERVERIP = "";
    public static int SERVERPORT = 8080;
    public static ServerThread SERVERTHREAD;
    public static ClientThread CLIENTTHREAD;
    public static ProgressDialog pdHost;
    public static boolean DONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp_prepare);

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.btnHost:
                        initHost();
                        break;
                    case R.id.btnClient:
                        initClient();
                        break;
                }
            }
        };
        btnHost = (Button) findViewById(R.id.btnHost);
        btnClient = (Button) findViewById(R.id.btnClient);
        serverIPText = (EditText) findViewById(R.id.editTextIP);

        btnHost.setOnClickListener(cl);
        btnClient.setOnClickListener(cl);
        
        uiHandler = new Handler();
        uiHandler.postDelayed(new Runnable() {                                                      //Wenn Client sagt, ich bin bereit, dann zu Layout wechseln
            @Override
            public void run() {
                boolean stop = false;
                if (DONE) {
                    Intent intent = new Intent(getBaseContext(), GameLayoutActivity.class);
                    intent.putExtra("mode", true);
                    intent.putExtra("server", true);
                    startActivity(intent);
                    stop = true;
                }
                if(!stop) {
                    uiHandler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(CLIENTTHREAD != null) {
            CLIENTTHREAD.stop();
        }
        if (SERVERTHREAD != null) {
            SERVERTHREAD.stop();
        }
    }

    private void initHost() {
        SERVERTHREAD = new ServerThread();
        SERVERTHREAD.setHdl(uiHandler);
        SERVERTHREAD.setActionCategory(0);
        Thread st = new Thread(SERVERTHREAD);
        st.start();
        pdHost = new ProgressDialog(this);
        if (getLocalIpAddress() != null) {
            pdHost.setMessage(getString(R.string.strSearchForPlayer) + "\n" + getString(R.string.strYourCode) + " " + getLocalIpAddress().substring(getLocalIpAddress().indexOf(".", 9) + 1));
        } else {
            pdHost.setMessage("Please check your network!");
        }
        pdHost.setCanceledOnTouchOutside(true);
        pdHost.show();
    }

    private void initClient() {
        if(getLocalIpAddress() != null & (!serverIPText.getText().toString().equals(""))) {
            SERVERIP = getLocalIpAddress().substring(0, getLocalIpAddress().indexOf(".", 9)) + "." + serverIPText.getText().toString();
        }
        if (!SERVERIP.equals("")) {
            CLIENTTHREAD = new ClientThread();
            Thread cThread = new Thread(CLIENTTHREAD);
            cThread.start();

            uiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        if(CLIENTTHREAD.getSocket() != null) {
                            CLIENTTHREAD.setAction("READY");
                            Intent intent = new Intent(getBaseContext(), GameLayoutActivity.class);
                            intent.putExtra("mode", true);
                            intent.putExtra("server", false);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getBaseContext(), "Can´t find host :(", Toast.LENGTH_SHORT).show();
                        }
                }
            }, 300);
        }
    }

    private String getLocalIpAddress() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

}

package com.example.tom.battleships;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button btnHost, btnClient, btnSend2Client, btnSendToServer;
    EditText serverIPText;
    public static String SERVERIP = "0.0.0.0";
    public static int SERVERPORT = 8080;
    public static ServerThread serverThread;
    public static ClientThread clientThread;
    public static ProgressDialog pdHost;

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
                case R.id.btnSendToClient:
                    PrintWriter outS;
                    try {
                        outS = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverThread.getSocket().getOutputStream())), true);
                        outS.println("Server is talking");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btnSendToServer:
                    PrintWriter outC;
                    try {
                        outC = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientThread.getSocket().getOutputStream())), true);
                        outC.println("IP" + "Client is talking");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
        btnHost = (Button) findViewById(R.id.btnHost);
        btnSend2Client = (Button) findViewById(R.id.btnSendToClient);
        btnClient = (Button) findViewById(R.id.btnClient);
        serverIPText = (EditText) findViewById(R.id.editTextIP);
        btnSendToServer = (Button) findViewById(R.id.btnSendToServer);

        btnHost.setOnClickListener(cl);
        btnClient.setOnClickListener(cl);
        btnSend2Client.setOnClickListener(cl);
        btnSendToServer.setOnClickListener(cl);

        btnSend2Client.setEnabled(false);
        btnSendToServer.setEnabled(false);
        uiHandler = new Handler();
    }

    private void initHost() {
        serverThread = new ServerThread();
        serverThread.setHdl(uiHandler);
        Thread st = new Thread(serverThread);
        st.start();
        pdHost = new ProgressDialog(this);
        pdHost.setMessage(getString(R.string.strSearchForPlayer) + "\n" + getString(R.string.strYourIPAddress) + " " + getLocalIpAddress());
        pdHost.setCanceledOnTouchOutside(true);
        pdHost.show();
        btnSend2Client.setEnabled(true);
    }

    private void initClient() {
        SERVERIP = serverIPText.getText().toString();
        if (!SERVERIP.equals("")) {
            clientThread = new ClientThread();
            Thread cThread = new Thread(clientThread);
            cThread.start();
        }
        btnSendToServer.setEnabled(true);
        //Intent startGameIntent = new Intent(this, GameActivity.class);
        //startGameIntent.putExtra("mode", 2);
        //startActivity(startGameIntent);
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

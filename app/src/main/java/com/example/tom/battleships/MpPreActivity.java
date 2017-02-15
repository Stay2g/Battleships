package com.example.tom.battleships;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Alrik on 10.02.2017.
 */

public class MpPreActivity extends MainMenuActivity {

    Button btnHost, btnClient;


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

        btnHost.setOnClickListener(cl);
        btnClient.setOnClickListener(cl);
    }

    private void initHost() {
        ProgressDialog pdHost = new ProgressDialog(this);
        pdHost.setMessage(getString(R.string.strSearchForPlayer) + "\n" + getString(R.string.strYourIPAddress) + " " + getLocalIpAddress());
        pdHost.setCanceledOnTouchOutside(true);
        pdHost.show();
    }

    private void initClient() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText ipAddress = new EditText(this);
        alert.setMessage("Please enter the IP-Address below:");
        alert.setView(ipAddress);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String YouEditTextValue = ipAddress.getText().toString();
            }
        });
        alert.show();
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

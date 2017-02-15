package com.example.tom.battleships;

import android.os.Handler;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alrik on 13.02.2017.
 * Alrik macht alles
 */

class ServerThread implements Runnable {
    private Socket socket = null;
    private Handler hdl;

    public void setHdl(Handler hdl) {
        this.hdl = hdl;
    }

    Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(MpPreActivity.SERVERPORT);
            Log.d("ServerThread", "ServerSocket");
            while(true) {
                socket = ss.accept();
                Log.d("ServerThread", "accept");
                if(hdl != null) {
                    hdl.post(new Runnable() {
                        @Override
                        public void run() {
                            MpPreActivity.pdHost.cancel();
                        }
                    });
                }
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.d("ServerThread", line);
                    }
                } catch (IOException e) {
                    Log.d("ServerThread", "Error@Reader");
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            Log.d("ServerThread", "Error@ServerSocket");
            e.printStackTrace();
        }

    }
}

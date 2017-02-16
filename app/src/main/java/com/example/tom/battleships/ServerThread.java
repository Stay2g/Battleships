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
    private Handler hdl = null;
    private int actionCategory = 0;

    void setActionCategory(int actionCategory) {
        this.actionCategory = actionCategory;
    }

    void setHdl(Handler hdl) {
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
                if(hdl != null & actionCategory == 0) {
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
                        switch(actionCategory) {
                            case 0:             //MpPreActivity
                                handlerPrepare(line);
                                break;
                            case 1:             //GameLayoutActivity
                                handlerLayout(line);
                                break;
                            case 2:             //GameActivity
                                handlerGame(line);
                        }
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

    private void handlerPrepare(String actionCode) {
        switch (actionCode) {
            case "READY":
                MpPreActivity.DONE = true;
        }
    }

    private void handlerLayout(String actionCode) {
        switch (actionCode) {
            case "READY":
                GameLayoutActivity.ENEMYREADY = true;
                break;
        }
    }

    private void handlerGame(String actionCode) {
        switch (actionCode) {
            case "OVER":
                break;
        }
    }
}

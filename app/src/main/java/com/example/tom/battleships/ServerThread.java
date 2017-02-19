package com.example.tom.battleships;

import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean allShipsRecived = false;
    private int[][] arrTextViewsEnemy= new int[8][5];
    private int enemyShot = -1;
    private boolean myTurn = true;
    private boolean sReady;
    private boolean runThread = true;

    public boolean issReady() {
        return sReady;
    }

    boolean isMyTurn() {
        return myTurn;
    }

    void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    int getEnemyShot() {
        return this.enemyShot;
    }

    int[][] getArrTextViewsEnemy() {
        return this.arrTextViewsEnemy;
    }

    boolean getAllShipsRecived() {
        return this.allShipsRecived;
    }

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
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(MpPreActivity.SERVERPORT);
            Log.d("ServerThread", "ServerSocket");
            while(runThread) {
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
        try {
            if (ss != null) {if (!ss.isClosed()) {
                ss.close();
            }}
            if(socket != null) { if (!socket.isClosed()) {
                    socket.close();
                }}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stop() {
        runThread = false;
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
            default:
                if (actionCode.substring(0,5).equals("SHIPS")) {
                    try {
                        JSONArray arrJson = new JSONArray(actionCode.substring(5));
                        for (int i = 0; i < arrJson.length(); i++) {
                            JSONObject objJson = arrJson.getJSONObject(i);
                            arrTextViewsEnemy[i][0] = objJson.getInt("textView1");
                            arrTextViewsEnemy[i][1] = objJson.getInt("textView2");
                            arrTextViewsEnemy[i][2] = objJson.getInt("textView3");
                            arrTextViewsEnemy[i][3] = objJson.getInt("textView4");
                            arrTextViewsEnemy[i][4] = objJson.getInt("textView5");
                        }
                    } catch (JSONException e) {
                        Log.d("JSON", "Can´t create JSONArray");
                        e.printStackTrace();
                    }
                    allShipsRecived = true;
                }
                break;
        }
    }

    private void handlerGame(String actionCode) {
        switch (actionCode) {
            case "READY":
                sReady = true;
                break;
            case "OVER":
                break;
            default:
                enemyShot = Integer.parseInt(actionCode);
                myTurn = false;
                break;
        }
    }
}

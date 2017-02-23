package com.example.tom.battleships;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    private boolean allShipsReceived = false;
    private int[][] arrTextViewsEnemy= new int[8][5];
    private int enemyShot = -1;
    private boolean runThread = true;
    private String action = null;
    private boolean canceled;


    @Override
    public void run() {
        Looper.prepare();
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(MainMenuActivity.SERVERPORT);
            Log.d("ServerThread", "ServerSocket");
            while(runThread) {
                socket = ss.accept();
                Log.d("ServerThread", "accept");
                if(hdl != null & actionCategory == 0) {
                    hdl.post(new Runnable() {
                        @Override
                        public void run() {
                            MainMenuActivity.pdHost.cancel();
                        }
                    });
                }
                Thread t = new Thread(new ServerThreadWriter());
                t.start();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.d("ServerThread", line);
                        switch(actionCategory) {
                            case 0:             //MainMenuActivity
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
            if (ss != null) { if (!ss.isClosed()) {
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
        try {
            if (socket != null) { if (socket.isClosed()) {
                    socket.close();
            }}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void setAction(String action) {
        this.action = action;
    }
    void setActionCategory(int actionCategory) {
        this.actionCategory = actionCategory;
    }
    void setHdl(Handler hdl) {
        this.hdl = hdl;
    }
    void setEnemyShot(int enemyShot) {
        this.enemyShot = enemyShot;
    }
    void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    boolean isCanceled() {
        return canceled;
    }
    int[][] getArrTextViewsEnemy() {
        return this.arrTextViewsEnemy;
    }
    int getEnemyShot() {
        return this.enemyShot;
    }
    boolean getAllShipsReceived() {
        return this.allShipsReceived;
    }
    Socket getSocket() {
        return socket;
    }

    private void handlerPrepare(String actionCode) {
        switch (actionCode) {
            case "READY":
                MainMenuActivity.DONE = true;
        }
    }
    private void handlerLayout(String actionCode) {
        switch (actionCode) {
            case "CANCEL":
                canceled = true;
                break;
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
                    allShipsReceived = true;
                }
                break;
        }
    }
    private void handlerGame(String actionCode) {
        switch (actionCode) {
            case "CANCEL":
                canceled = true;
                break;
            default:
                enemyShot = Integer.parseInt(actionCode);
                break;
        }
    }

    private class ServerThreadWriter implements Runnable {
        @Override
        public void run() {
            while (runThread) {
                if (action != null) {
                    PrintWriter outC;
                    try {
                        outC = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        outC.println(action);
                        action = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("ServerThreadWriter", "Error@PrintWriter");
                    }
                }
            }
        }
    }
}



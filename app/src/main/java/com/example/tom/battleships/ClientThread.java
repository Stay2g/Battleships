package com.example.tom.battleships;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;


class ClientThread implements Runnable {
    private int actionCategory = 1;
    private Socket socket = null;
    private boolean allShipsRecived = false;
    private int[][] arrTextViewsEnemy= new int[8][5];
    private int enemyShot = -1;
    private boolean myTurn;
    private boolean cReady;

    public boolean iscReady() {
        return cReady;
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

    Socket getSocket() {
        return socket;
    }

    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName(MpPreActivity.SERVERIP);
            Log.d("ClientActivity", "C: Connecting...");
            socket = new Socket(serverAddr, MpPreActivity.SERVERPORT);
            while (true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.d("ClientThread", line);
                        switch (actionCategory) {
                            case 1:                     //GameLayoutActivity
                                handlerLayout(line);
                                break;
                            case 2:                     //GameActivity
                                handlerGame(line);
                                break;
                        }
                    }
                } catch (IOException e) {
                    Log.d("ClientThread", "Error@Reader");
                    e.printStackTrace();
                    break;
                }
            }
            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.e("ClientActivity", "C: Error", e);
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
                cReady = true;
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

package com.example.tom.battleships;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


class ClientThread implements Runnable {
    private int actionCategory = 1;
    private Socket socket = null;
    private boolean allShipsReceived = false;
    private int[][] arrTextViewsEnemy= new int[8][5];
    private int enemyShot = -1;
    private boolean runThread = true;
    private String action = null;
    private boolean canceled;

    public void run() {
        try {
            Looper.prepare();
            InetAddress serverAddr = InetAddress.getByName(MainMenuActivity.SERVERIP);
            Log.d("ClientActivity", "C: Waiting for Client...");
            socket = new Socket(serverAddr, MainMenuActivity.SERVERPORT);
            Thread t = new Thread(new ClientThreadWriter());
            t.start();
            while (runThread) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.d("ClientThread", "Incoming message: " + line);
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
            Log.d("ClientThread", "Closed.");
        } catch (Exception e) {
            Log.e("ClientThread", "Error", e);
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

    void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
    void setAction(String action) {
        this.action = action;
    }
    void setActionCategory(int actionCategory) {
        this.actionCategory = actionCategory;
    }
    Socket getSocket() {
        return socket;
    }
    void setEnemyShot(int enemyShot) {
        this.enemyShot = enemyShot;
    }

    boolean isCanceled() {
        return canceled;
    }
    int getEnemyShot() {
        return this.enemyShot;
    }
    int[][] getArrTextViewsEnemy() {
        return this.arrTextViewsEnemy;
    }
    boolean getAllShipsReceived() {
        return this.allShipsReceived;
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

    private class ClientThreadWriter implements Runnable {
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
                        Log.d("ClientThreadWriter", "Error@PrintWriter");
                    }
                }
            }
        }
    }
}

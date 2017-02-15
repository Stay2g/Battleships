package com.example.tom.battleships;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


class ClientThread implements Runnable {
    private Socket socket = null;

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
}

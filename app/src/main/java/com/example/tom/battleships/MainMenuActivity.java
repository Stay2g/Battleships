package com.example.tom.battleships;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class MainMenuActivity extends BaseActivity {
    Handler h;
    TextView textViewLoggedInUser;
    EditText etCode;
    Button btnMultiplayer, btnSingleplayer, btnSettings, btnLogout, btnHelp, btnClient, btnServer;
    public static String SERVERIP = "";
    public static int SERVERPORT = 8080;
    public static ServerThread SERVERTHREAD;
    public static ClientThread CLIENTTHREAD;
    public static ProgressDialog pdHost;
    public static boolean DONE;
    public static boolean SERVER, MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        String username = getIntent().getStringExtra("strUsername");
        textViewLoggedInUser = (TextView) findViewById(R.id.textViewUsername);
        textViewLoggedInUser.setText(username);

        etCode = (EditText) findViewById(R.id.editTextCode);
        btnServer = (Button) findViewById(R.id.btnServer);
        btnClient = (Button) findViewById(R.id.btnClient);
        btnSingleplayer = (Button) findViewById(R.id.btnSingleplayer);
        btnMultiplayer = (Button) findViewById(R.id.btnMultiplayer);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnHelp = (Button) findViewById(R.id.btnHelp);

        btnSingleplayer.setOnClickListener(this);
        btnMultiplayer.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnClient.setOnClickListener(this);
        btnServer.setOnClickListener(this);
        btnHelp.setOnClickListener(this);

        h = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSingleplayer:
                Intent startGameLayout = new Intent(this, GameLayoutActivity.class);
                startGameLayout.putExtra("mode", false);
                startActivity(startGameLayout);
                break;

            case R.id.btnMultiplayer:
                animButtons();
                h.postDelayed(new Runnable() {                                                      //Wenn Client sagt, ich bin bereit, dann zu Layout wechseln
                @Override
                public void run() {
                    boolean stop = false;
                    if (DONE) {
                        Intent intent = new Intent(MainMenuActivity.this, GameLayoutActivity.class);
                        SERVER = true;
                        MODE = true;
                        intent.putExtra("mode", true);
                        intent.putExtra("server", true);
                        startActivity(intent);
                        stop = true;
                    }
                    if(!stop) {
                        h.postDelayed(this, 500);
                    }
                }
            }, 500);
                break;

            case R.id.btnSettings:
                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View dialogView = layoutInflater.inflate(R.layout.dialog_settings, null);
                AlertDialog.Builder dialogSettings = new AlertDialog.Builder(this);
                dialogSettings.setView(dialogView);
                dialogSettings.setPositiveButton(R.string.strSave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialogSettings.setNegativeButton(R.string.strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialogSettings.show();
                break;

            case R.id.btnLogout:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.btnServer:
                initHost();
                break;
            case R.id.btnClient:
                initClient();
                break;
            case R.id.btnHelp:
                final AlertDialog.Builder dialogHelp = new AlertDialog.Builder(this);
                dialogHelp.setTitle(R.string.strHelpTitle);
                dialogHelp.setMessage(R.string.strHelp);
                dialogHelp.setPositiveButton(R.string.strOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialogHelp.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CLIENTTHREAD != null) {
            CLIENTTHREAD.setAction("CANCEL");
            CLIENTTHREAD.setCanceled(true);
            CLIENTTHREAD.stop();
        }
        if (SERVERTHREAD != null) {
            SERVERTHREAD.setAction("CANCEL");
            SERVERTHREAD.setCanceled(true);
            SERVERTHREAD.stop();
        }
    }

    // ---- Multiplayer

    private void animButtons() {
        btnMultiplayer.setAlpha(0.0f);
        btnServer.setVisibility(View.VISIBLE);
        btnClient.setVisibility(View.VISIBLE);
        btnClient.setEnabled(false);
        etCode.setVisibility(View.VISIBLE);

        h.post(new Runnable() {
            @Override
            public void run() {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setDuration(400);
                etCode.startAnimation(fadeIn);
                btnServer.startAnimation(fadeIn);
                btnClient.startAnimation(fadeIn);
                etCode.setAlpha(1.0f);
                btnClient.setAlpha(1.0f);
                btnServer.setAlpha(1.0f);
            }
        });

        btnClient.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!etCode.getText().toString().equals("")) {
                    btnClient.setEnabled(true);
                } else {
                    btnClient.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void initHost() {
        SERVERTHREAD = new ServerThread();
        SERVERTHREAD.setHdl(h);
        SERVERTHREAD.setActionCategory(0);
        Thread st = new Thread(SERVERTHREAD);
        st.start();
        pdHost = new ProgressDialog(this);
        if (getLocalIpAddress() != null) {
            pdHost.setMessage(getString(R.string.strWaitingForPlayer) + "\n" + getString(R.string.strYourCode) + " " + getLocalIpAddress().substring(getLocalIpAddress().indexOf(".", 9) + 1));
        } else {
            pdHost.setMessage(getString(R.string.strCheckNetwork));
        }
        pdHost.setCanceledOnTouchOutside(true);
        pdHost.show();
    }

    private void initClient() {
        if(getLocalIpAddress() != null & (!etCode.getText().toString().equals(""))) {
            SERVERIP = getLocalIpAddress().substring(0, getLocalIpAddress().indexOf(".", 9)) + "." + etCode.getText().toString();
        }
        if (!SERVERIP.equals("") & !SERVERIP.equals(getLocalIpAddress())) {
            CLIENTTHREAD = new ClientThread();
            CLIENTTHREAD.setActionCategory(1);
            Thread cThread = new Thread(CLIENTTHREAD);
            cThread.start();

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(CLIENTTHREAD.getSocket() != null) {
                        CLIENTTHREAD.setAction("READY");
                        Intent intent = new Intent(getBaseContext(), GameLayoutActivity.class);
                        intent.putExtra(getString(R.string.strMode), true);
                        intent.putExtra(getString(R.string.strServer), false);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getBaseContext(), R.string.strHostNotFound, Toast.LENGTH_SHORT).show();
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

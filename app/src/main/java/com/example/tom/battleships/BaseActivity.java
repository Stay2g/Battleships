package com.example.tom.battleships;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class BaseActivity extends FragmentActivity implements View.OnClickListener {

    //Wert verändern für direkten Spielstart:
    //true -> Login wird Umgangen
    //false  -> Loginfenster ist der start
    boolean instantGameStart = false;

    private static final int RC_LOGOUT = 1;
    private static final String TAG = "SignInActivity";

    private ImageView logo;
    private TextView logoColor;
    private EditText editTextLoginName, editTextLoginPassword;
    Button btnLogin, btnReg, btnGuestLogin;

    private DBAdapter dbAdapter;

    public String strLoginName, strLoginPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initAll();
        initLogo();
        btnLogin.setVisibility(View.GONE);
        btnGuestLogin.setVisibility(View.GONE);
        btnReg.setVisibility(View.GONE);
        findViewById(R.id.logo2).bringToFront();                                                   //initialisiere alle Buttons, TextViews, usw.
        findViewById(R.id.logo1).bringToFront();

        if (instantGameStart) {                                                                     //loginUmgehung == true -> Loginanzeige wird übersprungen
            Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
            startActivity(intentStartGame);
        }

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        try {
            Cursor cursor = dbAdapter.getName(1);                                                   //1. Wert aus der Datenbank
            String firstDbEntry = cursor.getString(0);                                              //Namensstring entnehmen
            Log.d("DB EINTRAG", firstDbEntry);
        } catch (Exception e) {
            Log.d("EXCEPTION", "NEUER DATENBANKEINTRAG WIRD ANGELEGT");                             //Falls der erste User nicht der ADMIN ist, wird er eingetragen
            dbAdapter.insertNewUser("ADMIN", "ADMIN");                                              //-> Erstbenutzung der App für Login ohne Registrierung
        }

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ColorDrawable[] cd = {new ColorDrawable(Color.BLACK), new ColorDrawable(Color.WHITE)};
                TransitionDrawable trans = new TransitionDrawable(cd);
                logo.setBackground(trans);
                trans.startTransition(2000);
            }
        },1000);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
                aa.setInterpolator(new DecelerateInterpolator());
                aa.setDuration(800);
                logoColor.startAnimation(aa);
                logo.startAnimation(aa);

                logoColor.setVisibility(View.GONE);
                logo.setVisibility(View.GONE);

                btnLogin.setVisibility(View.VISIBLE);
                btnGuestLogin.setVisibility(View.VISIBLE);
                btnReg.setVisibility(View.VISIBLE);
            }
        }, 4000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();                                                                          //Datenbankverbindung beenden
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                userDataCheck();
                break;

            case R.id.btnRegisterCancel:
                Intent intentStartRegAct = new Intent(this, RegisterActivity.class);
                startActivity(intentStartRegAct);
                break;

            case R.id.btnGuestLogin:
                editTextLoginName.setText("ADMIN");                                                 //'ADMIN' 'ADMIN' in EditTexts setzen
                editTextLoginPassword.setText("ADMIN");
                userDataCheck();
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            getWindow().getDecorView().setSystemUiVisibility(
                              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_LOGOUT) {

            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, R.string.strLogoutCompleted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void userDataCheck() {

        strLoginName = editTextLoginName.getText().toString();
        strLoginPassword = editTextLoginPassword.getText().toString();

        boolean userDataCorrect = dbAdapter.checkUserData(strLoginName, strLoginPassword);          //Prüfe ob Name mit passendem Passwort in Datenbank eingetragen ist

        if (strLoginName.isEmpty() && strLoginPassword.isEmpty()) {                                  //Bei Leeren Textfeldern -> Fehler
            Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else if (userDataCorrect) {                                                                //Bei Falschen Daten: userDataCorrect == False
            Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else {                                                                                    //Rest ist passend
            Intent intentStartGame = new Intent(this, MainMenuActivity.class);
            intentStartGame.putExtra("strUsername", strLoginName);
            startActivityForResult(intentStartGame, RC_LOGOUT);
        }
    }

    private void initAll() {

        editTextLoginName = (EditText) findViewById(R.id.editTextLogin);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextPwd);

        logo = (ImageView) findViewById(R.id.logo1);
        logoColor = (TextView) findViewById(R.id.logo2);

        btnReg = (Button) findViewById(R.id.btnRegisterCancel);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGuestLogin = (Button) findViewById(R.id.btnGuestLogin);

        btnReg.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnGuestLogin.setOnClickListener(this);
    }

    private void initLogo() {
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.av);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;

        ImageView iv = (ImageView) findViewById(R.id.logo1);
        iv.setImageBitmap(Bitmap.createScaledBitmap(background, w, h, false));
    }
}
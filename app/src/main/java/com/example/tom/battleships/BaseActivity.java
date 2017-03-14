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
import android.view.Window;
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

    private EditText editTextLoginName, editTextLoginPassword;
    Button btnLogin, btnReg, btnGuestLogin;

    private DBAdapter dbAdapter;

    public String strLoginName, strLoginPassword;

    Intent musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);


        musicService = new Intent(this, BackgroundSoundService.class);
        startService(musicService);

        initAll();

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();                                                                          //Datenbankverbindung beenden
        stopService(musicService);
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

        btnReg = (Button) findViewById(R.id.btnRegisterCancel);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGuestLogin = (Button) findViewById(R.id.btnGuestLogin);

        btnReg.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnGuestLogin.setOnClickListener(this);
    }
}
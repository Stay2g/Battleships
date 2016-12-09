package com.example.tom.battleships;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class BaseActivity extends FragmentActivity implements View.OnClickListener{

    //Wert verändern für direkten Spielstart:
    //true -> Login wird Umgangen
    //false  -> Loginfenster ist der start
    private boolean instantGameStart = false;

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "SignInActivity";

    private ProgressBar pbBar;

    private EditText editTextLoginName, editTextLoginPassword;
    private Button btnLogin, btnReg, btnGuestLogin, btnSingleplayer, btnMultiplayer, btnSettings;

    private DBAdapter dbAdapter;
    private SignInButton mGoogleBtn;
    private GoogleApiClient mGoogleApiClient;

    public String strLoginName, strLoginPassword;

    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initAll();                                                                                  //initialisiere alle Buttons, TextViews, usw.

        if (instantGameStart) {                                                                     //loginUmgehung == true -> Loginanzeige wird übersprungen
            Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
            startActivity(intentStartGame);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions                                           //Accountwahl-Dialog wird gebaut
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(BaseActivity.this, "Error", Toast.LENGTH_SHORT).show();      //Keine Verbindung
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(BaseActivity.this, "Logged In", Toast.LENGTH_SHORT).show();      //User logged in

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");                                    //User logged out
                }
            }
        };

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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        com.example.tom.battleships.SystemUiHelper uiHelper =                                       //Aufrufen einer Helper-Klasse, die einen "richtigen" Vollbildmodus hervorruft
                new com.example.tom.battleships.SystemUiHelper(
                        this, com.example.tom.battleships.SystemUiHelper.LEVEL_IMMERSIVE ,flags);
        uiHelper.hide();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();                                                                          //Datenbankverbindung beenden
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin:
                mAuth.signOut();
                editTextLoginName.setText("ADMIN");                                                 //'ADMIN' 'ADMIN' in EditTexts setzen
                editTextLoginPassword.setText("ADMIN");
                userDataCheck();
                finish();
                break;
            case R.id.btnRegister:
                Intent intentStartRegAct = new Intent(this, RegisterActivity.class);
                startActivity(intentStartRegAct);
                break;
            case R.id.btnGuestLogin:

                break;
            case R.id.sign_in_button:
                signIn();                                                                           //Start des Google-Login-Vorgangs
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {                                                            //Antwort vom Login Intent
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();                            //Falls Login erfolgreich -> Datenentnahme des eigenloggten accounts
                firebaseAuthWithGoogle(account);
                pbBar.setVisibility(View.GONE);
                Intent intent = new Intent(this, MainMenuActivity.class);
                startActivity(intent);
                finish();
            } else {
                pbBar.setVisibility(View.GONE);
            }
        }
    }

    private void userDataCheck() {

        strLoginName = editTextLoginName.getText().toString();
        strLoginPassword = editTextLoginPassword.getText().toString();

        boolean userDataCorrect = dbAdapter.checkUserData(strLoginName, strLoginPassword);          //Prüfe ob Name mit passendem Passwort in Datenbank eingetragen ist

        if (strLoginName.isEmpty() && strLoginPassword.isEmpty()){                                  //Bei Leeren Textfeldern -> Fehler
            Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else if (userDataCorrect){                                                                //Bei Falschen Daten: userDataCorrect == False
             Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else {                                                                                    //Rest ist passend
            Intent intentStartGame = new Intent(this, MainMenuActivity.class);
            startActivity(intentStartGame);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        pbBar.setVisibility(View.VISIBLE);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(BaseActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    private void initAll() {                                                                        //Set GUI-Elemente und OnClickListener
        pbBar = (ProgressBar) findViewById(R.id.loadingProgress);

        editTextLoginName = (EditText) findViewById(R.id.editTextLogin);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextPwd);

        btnReg = (Button) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGuestLogin = (Button) findViewById(R.id.btnGuestLogin);
        btnSingleplayer = (Button) findViewById(R.id.btnSingleplayer);
        btnMultiplayer = (Button) findViewById(R.id.btnMultiplayer);
        //btnSettings = (Button) findViewById(R.id.btnSettings);
        mGoogleBtn = (SignInButton) findViewById(R.id.sign_in_button);

        btnReg.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnGuestLogin.setOnClickListener(this);
        btnSingleplayer.setOnClickListener(this);
        btnMultiplayer.setOnClickListener(this);
        //btnSettings.setOnClickListener(this);
        mGoogleBtn.setOnClickListener(this);
    }
}
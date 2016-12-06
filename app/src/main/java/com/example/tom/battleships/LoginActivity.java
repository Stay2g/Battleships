package com.example.tom.battleships;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
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

public class LoginActivity extends FragmentActivity implements View.OnClickListener{

    //Ändere den Wert wenn du den Login ausprobieren willst:
    //false -> Login wird Umgangen
    //true  -> Loginfenster ist der start
    private boolean loginUmgehung = true;

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "SignInActivity";

    private ImageView imageViewProfilePicture;
    private TextView textViewUsername;

    private EditText editTextLoginName, editTextLoginPassword;
    private Button btnLogin, btnReg;

    private DBAdapter dbAdapter;
    private SignInButton mGoogleBtn;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        initAll();

        if (loginUmgehung == true){
            Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
            startActivity(intentStartGame);
        }

        mGoogleBtn = (SignInButton) findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        /*
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        try {
            Cursor cursor = dbAdapter.getName(1);
            String firstDbEntry = cursor.getString(0);
            Log.d("DB EINTRAG", firstDbEntry);
        } catch (Exception e){
            Log.d("EXCEPTION", "NEUER DATENBANKEINTRAG WIRD ANGELEGT");
            dbAdapter.insertNewUser("ADMIN", "ADMIN");
        }
        */
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin:
                editTextLoginName.setText("ADMIN");
                editTextLoginPassword.setText("ADMIN");
                Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
                startActivity(intentStartGame);
                //userDataCheck();
                break;
            case R.id.btnRegister:
                Intent intentStartRegAct = new Intent(this, RegisterActivity.class);
                startActivity(intentStartRegAct);
                break;
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }


    private void userDataCheck() {

        String strLoginName = editTextLoginName.getText().toString();
        String strLoginPassword = editTextLoginPassword.getText().toString();
        boolean userDataCorrect = dbAdapter.checkUserData(strLoginName, strLoginPassword);

        if (strLoginName.isEmpty() && strLoginPassword.isEmpty()){
            Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else if (userDataCorrect){
            Toast.makeText(this, R.string.strLoginFailed, Toast.LENGTH_SHORT).show();
        } else {
            Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
            startActivity(intentStartGame);
        }
    }

    private void initAll() {
        imageViewProfilePicture = (ImageView) findViewById(R.id.imageViewProfilePicture);

        textViewUsername = (TextView) findViewById(R.id.textViewUsername);

        editTextLoginName = (EditText) findViewById(R.id.editTextLogin);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextPwd);

        btnReg = (Button) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        //signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        //signInButton.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

}

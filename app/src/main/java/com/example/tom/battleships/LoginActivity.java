package com.example.tom.battleships;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener{

    DBAdapter dbAdapter;
    EditText editTextLoginName, editTextLoginPassword;
    Button btnLogin, btnReg, btnAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

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

        editTextLoginName = (EditText) findViewById(R.id.editTextLogin);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextPwd);
        btnReg = (Button) findViewById(R.id.btnRegister);
        btnAutoLogin = (Button) findViewById(R.id.btnAutoLogin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAutoLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin:
                userDataCheck();
                break;
            case R.id.btnRegister:
                Intent intentStartRegAct = new Intent(this, RegisterActivity.class);
                startActivity(intentStartRegAct);
                break;
            case R.id.btnAutoLogin:
                editTextLoginName.setText("ADMIN");
                editTextLoginPassword.setText("ADMIN");
                userDataCheck();
                break;
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
}

package com.example.tom.battleships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class LoginActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        Button btnReg = (Button) findViewById(R.id.btnRegister);
        Button btnAutoLogin = (Button) findViewById(R.id.btnAutoLogin);
        btnReg.setOnClickListener(this);
        btnAutoLogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRegister:
                Intent intentStartRegAct = new Intent(this, RegisterActivity.class);
                startActivity(intentStartRegAct);
                break;
            case R.id.btnAutoLogin:
                Intent intentStartGame = new Intent(this, GameLayoutActivity.class);
                startActivity(intentStartGame);
                break;
        }
    }
}

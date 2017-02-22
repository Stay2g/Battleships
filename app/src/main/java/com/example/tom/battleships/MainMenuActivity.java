package com.example.tom.battleships;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainMenuActivity extends BaseActivity{

    TextView textViewLoggedInUser;
    EditText etCode;
    Button btnMultiplayer, btnSingleplayer, btnSettings, btnLogout, btnClient, btnServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        btnSingleplayer.setOnClickListener(this);
        btnMultiplayer.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnClient.setOnClickListener(this);
        btnServer.setOnClickListener(this);
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
                //Intent intent = new Intent(this, MpPreActivity.class);
                //startActivity(intent);
                animButtons();
                break;

            case R.id.btnSettings:
                break;

            case R.id.btnLogout:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.btnServer:
                break;
            case R.id.btnClient:
                break;
        }
    }

    private void animButtons() {
        btnMultiplayer.setAlpha(0.0f);
        btnServer.setVisibility(View.VISIBLE);
        btnClient.setVisibility(View.VISIBLE);
        btnClient.setEnabled(false);
        etCode.setVisibility(View.VISIBLE);
        Handler h = new Handler();
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


}

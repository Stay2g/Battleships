package com.example.tom.battleships;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainMenuActivity extends BaseActivity{

    TextView textViewLoggedInUser;
    Button btnMultiplayer, btnSingleplayer, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        String username = getIntent().getStringExtra("strUsername");
        textViewLoggedInUser = (TextView) findViewById(R.id.textViewUsername);
        textViewLoggedInUser.setText(username);

        btnSingleplayer = (Button) findViewById(R.id.btnSingleplayer);
        btnMultiplayer = (Button) findViewById(R.id.btnMultiplayer);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnSingleplayer.setOnClickListener(this);
        btnMultiplayer.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
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
                Intent intent = new Intent(this, MpPreActivity.class);
                startActivity(intent);
                break;

            case R.id.btnSettings:
                break;

            case R.id.btnLogout:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;

        }
    }
}

package com.example.tom.battleships;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Kaden on 08.12.2016.
 */

public class MainMenuActivity extends BaseActivity{

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        textView = (TextView) findViewById(R.id.textViewUsername);

        if (mAuth.getCurrentUser() != null) {
            textView.setText(mAuth.getCurrentUser().getDisplayName());
        } else if (!strLoginName.isEmpty()){
            textView.setText(strLoginName);
        }
    }

    @Override
    public void onClick(View view) {
        /*
        switch (view.getId()){
            case R.id.btnSingleplayer:
                Intent startShipSettingIntent = new Intent(this, GameLayoutActivity.class);
                startActivity(startShipSettingIntent);
                break;
            case R.id.btnMultiplayer:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getString(R.string.strSearchForPlayer));
                progressDialog.show();
                searchForPlayer();
                break;

            case R.id.btnSettings:
                break;
        }
        */
    }

    private void searchForPlayer() {
        //TODO: Spielersuche
        //per Bluetooth?
        //
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
}

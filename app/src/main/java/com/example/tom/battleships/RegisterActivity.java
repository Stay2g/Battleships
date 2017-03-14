package com.example.tom.battleships;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity{

    Button btnCofirm, btnCancel;
    EditText editTextName, editTextPassword, editTextPasswordConfirm;
    DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        editTextName = (EditText) findViewById(R.id.editTextRegName);
        editTextPassword = (EditText) findViewById(R.id.editTextRegPwd);
        editTextPasswordConfirm = (EditText) findViewById(R.id.editTextRegPwdRpt);

        btnCofirm = (Button) findViewById(R.id.btnRegisterConfirm);
        btnCancel = (Button) findViewById(R.id.btnRegisterCancel);
        btnCofirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRegisterConfirm:
                String username = editTextName.getText().toString();
                String password = editTextPassword.getText().toString();

                if (editTextName.length() > 0) {
                    if (!dbAdapter.checkForName(username)) {
                        if (editTextPassword.length() > 0) {
                            if (editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString())) {
                                dbAdapter.insertNewUser(username, password);
                                Log.d("DB New User", username + " " + password);
                                finish();
                                Toast.makeText(this, R.string.strUserCreated, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, R.string.strPwdDontMatch, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, R.string.strPwdNeeded, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.strNameTaken, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.strNoName, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRegisterCancel:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}

package com.example.tom.battleships;

import android.os.Bundle;
import android.widget.EditText;

public class RegisterActivity extends BaseActivity {

    EditText editTextUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
}

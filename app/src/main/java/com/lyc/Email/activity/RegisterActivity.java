package com.lyc.Email.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lyc.Email.R;
import com.lyc.Email.listener.RegisterListener;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RegisterListener rl = new RegisterListener(this);
        findViewById(R.id.registerSubmit).setOnClickListener(rl);
    }
}

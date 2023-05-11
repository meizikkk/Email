package com.lyc.Email.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lyc.Email.R;
import com.lyc.Email.listener.LoginListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginListener lg = new LoginListener(this);
        findViewById(R.id.login).setOnClickListener(lg);
        findViewById(R.id.register).setOnClickListener(lg);
    }
}
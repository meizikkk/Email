package com.lyc.Email.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lyc.Email.R;
import com.lyc.Email.listener.SendMessageListener;

public class SendMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("mail");

        TextView sender = findViewById(R.id.senderName);
        sender.setText(msg);

        SendMessageListener sml = new SendMessageListener(this,msg);
        findViewById(R.id.sendMsg).setOnClickListener(sml);
    }
}

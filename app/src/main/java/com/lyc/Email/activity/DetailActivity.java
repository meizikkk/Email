package com.lyc.Email.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lyc.Email.R;

import org.json.JSONException;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView senderName = findViewById(R.id.detailSender);
        TextView title = findViewById(R.id.receiveTitleName);
        TextView content = findViewById(R.id.receiveContent);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
//        System.out.println(id);
        try {
            senderName.setText(IndexActivity.data.getJSONObject(Integer.parseInt(id) - 800).get("emailFrom").toString());
            title.setText(IndexActivity.data.getJSONObject(Integer.parseInt(id) - 800).get("title").toString());
            content.setText(IndexActivity.data.getJSONObject(Integer.parseInt(id) - 800).get("content").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.lyc.Email.listener;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.activity.MainActivity;
import com.lyc.Email.R;
import com.lyc.Email.activity.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginListener implements View.OnClickListener {

    private MainActivity mainActivity;

    public LoginListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register){
            Intent intent = new Intent(mainActivity, RegisterActivity.class);
            mainActivity.startActivity(intent);
            if (RegisterListener.registerSuccess) {
                mainActivity.finish();
                RegisterListener.registerSuccess = false;
            }
        }
        if (v.getId() == R.id.login) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();//增加部分
                        Connect();
                        Looper.loop();//增加部分
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void Connect() throws IOException, JSONException {
        TextView email = mainActivity.findViewById(R.id.editTextTextEmailAddress);
        TextView password = mainActivity.findViewById(R.id.editTextTextPassword);
        String urlPath = "http://192.168.43.48:8088/users/login";
//        String urlPath = "http://124.222.180.235:8088/users/login";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("mail", email.getText());
        data.put("password", password.getText());
        String content = data.toString();
        OutputStream os = conn.getOutputStream();
        os.write(content.getBytes()); //字符串写进二进流
        os.close();

        // 获取得到的json数据
        InputStream inputStream = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        JSONObject result = new JSONObject(String.valueOf(response));

//        System.out.println(result.get("code"));
        if (result.get("code").equals(1)) {
            Intent intent = new Intent(mainActivity, IndexActivity.class);
            intent.putExtra("email",email.getText().toString());
            intent.putExtra("password",password.getText().toString());
            mainActivity.startActivity(intent);
            mainActivity.finish();
        } else {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("提示")
                    .setMessage("用户不存在或密码错误！！！")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
}

package com.lyc.Email.listener;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

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
import java.util.regex.Pattern;

public class RegisterListener implements View.OnClickListener {

    private RegisterActivity registerActivity;

    public static boolean registerSuccess = false;

    public RegisterListener(RegisterActivity registerActivity) {
        this.registerActivity = registerActivity;
    }

    @Override
    public void onClick(View v) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Looper.prepare();//增加部分
                    Connect();
                    Looper.loop();//增加部分
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void Connect() throws IOException, JSONException, InterruptedException {
        TextView email = registerActivity.findViewById(R.id.editTextTextEmailAddress);
        TextView password = registerActivity.findViewById(R.id.editTextTextPassword);
        String urlPath = "http://192.168.43.48:8088/users";
//        String urlPath = "http://124.222.180.235:8088/users";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();

        if (!isValidEmail(email.getText().toString()) || password.getText().toString().equals("")) {
            new AlertDialog.Builder(registerActivity)
                    .setTitle("提示")
                    .setMessage("用户名格式不规范!\n@前为3-18个英文字符\n@后必须为yc.com\n且密码不能为空")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }


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
            registerSuccess = true;
            Intent intent = new Intent(registerActivity, MainActivity.class);
            registerActivity.startActivity(intent);
            registerActivity.finish();

        } else {
            new AlertDialog.Builder(registerActivity)
                    .setTitle("提示")
                    .setMessage("该用户名已存在！！！")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    public static boolean isValidEmail(String email) {
        if ((email != null) && (!email.isEmpty())) {
            return Pattern.matches("^(\\w+([-.][A-Za-z]+)*){3,8}@yc.com", email);
        }
        return false;
    }

}

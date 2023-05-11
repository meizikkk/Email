package com.lyc.Email.listener;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.R;
import com.lyc.Email.activity.SendMessageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendMessageListener implements View.OnClickListener{

    private SendMessageActivity sendMessageActivity;

    private String emailFrom;

    public SendMessageListener(SendMessageActivity sendMessageActivity,String emailFrom) {
        this.sendMessageActivity = sendMessageActivity;
        this.emailFrom = emailFrom;
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
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void Connect() throws IOException, JSONException {
        TextView emailTo = sendMessageActivity.findViewById(R.id.receiverName);
        TextView title = sendMessageActivity.findViewById(R.id.titleName);
        TextView sendContent = sendMessageActivity.findViewById(R.id.sendContent);
        String urlPath = "http://192.168.43.48:8088/users/sendMail";
//        String urlPath = "http://124.222.180.235:8088/users/sendMail";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("emailFrom",emailFrom);
        data.put("emailTo", emailTo.getText().toString());
        if (emailTo.getText().toString().equals(emailFrom)) {
            new AlertDialog.Builder(sendMessageActivity)
                    .setTitle("提示")
                    .setMessage("不能给自己发邮件！")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }


        if (title.getText().toString().equals("") || sendContent.getText().toString().equals("")) {
            new AlertDialog.Builder(sendMessageActivity)
                    .setTitle("提示")
                    .setMessage("请输入标题或文章内容！")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }
        data.put("title", title.getText().toString());
        data.put("content", sendContent.getText().toString());
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
            IndexActivity.flag = true;
            Intent intent = new Intent(sendMessageActivity, IndexActivity.class);
            intent.putExtra("email",emailFrom);
            sendMessageActivity.startActivity(intent);
            sendMessageActivity.finish();
        }
        if (result.get("msg").equals("没有该用户!")) {
            new AlertDialog.Builder(sendMessageActivity)
                    .setTitle("提示")
                    .setMessage("没有该用户")
                    .setPositiveButton("确定", null)
                    .show();
        }
        else if (result.get("msg").equals("未知错误")){
            new AlertDialog.Builder(sendMessageActivity)
                    .setTitle("提示")
                    .setMessage("发送失败！！！\n请检查网络！！！")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
}

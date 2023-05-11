package com.lyc.Email.listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lyc.Email.R;
import com.lyc.Email.activity.GroupChatActivity;
import com.lyc.Email.activity.GroupListActivity;
import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.utils.BitMapImage;
import com.lyc.Email.utils.PhotoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupChatListener implements View.OnClickListener {

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private GroupChatActivity groupChatActivity;

    private String emailFrom;

    private String imgString = "";

    private int groupId;

    public GroupChatListener(GroupChatActivity groupChatActivity, String emailFrom, int groupId) {
        this.groupChatActivity = groupChatActivity;
        this.emailFrom = emailFrom;
        this.groupId = groupId;
    }


    @Override
    public void onClick(View v) {
        GroupChatActivity.data = null;
        if (v.getId() == R.id.sendMsg) {
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
        } else if (v.getId() == R.id.upLoadImage) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            groupChatActivity.setIntent(intent);
            groupChatActivity.setResult(Activity.RESULT_OK, intent);
            Uri uri = intent.getData();
            Bitmap bitmap = PhotoUtils.getBitmapFromUri(uri,groupChatActivity);
            imgString = BitMapImage.bitmapToBase64(bitmap);

        }

    }

    public void Connect() throws IOException, JSONException {
        TextView sendContent = groupChatActivity.findViewById(R.id.group_send);
        TextView groupId = groupChatActivity.findViewById(R.id.GroupName);

        String urlPath = "http://192.168.43.48:8088/group/sendGroupMail";
//        String urlPath = "http://124.222.180.235:8088/group/sendGroupMail";
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(50000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("groupId", groupId.getText());
        data.put("messageFrom", emailFrom);
        data.put("sendContent", sendContent.getText());
        String content = data.toString();
        OutputStream os = conn.getOutputStream();
        String new_content = new String(content.getBytes());
        System.out.println("new_content: " + new_content);
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

        groupChatActivity.runOnUiThread(() -> {
            @SuppressLint("CutPasteId") TextView textView = groupChatActivity.findViewById(R.id.group_send);
            textView.setText("");
        });


    }
}

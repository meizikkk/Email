package com.lyc.Email.listener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.lyc.Email.R;
import com.lyc.Email.activity.DetailActivity;
import com.lyc.Email.activity.GroupChatActivity;
import com.lyc.Email.activity.GroupListActivity;
import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.activity.SendMessageActivity;
import com.lyc.Email.thread.GroupListThread;
import com.lyc.Email.thread.IndexThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GroupListListener implements View.OnClickListener {
    private GroupListActivity groupListActivity;

    private FrameLayout frameLayout;

    public static boolean flush = false;

    private String inputName;

    private String msg;

    private JSONArray data = new JSONArray();

    public GroupListListener(GroupListActivity groupListActivity, String msg, FrameLayout frameLayout) {
        this.groupListActivity = groupListActivity;
        this.msg = msg;
        this.frameLayout = frameLayout;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.sendMail) {
            Intent intent = new Intent(groupListActivity, SendMessageActivity.class);
            intent.putExtra("mail",msg);
            groupListActivity.startActivity(intent);
        } else if (v.getId() == R.id.flush) {
//            System.out.println("点击");
            flush = true;
            GroupListThread.flag = true;
        } else if (v.getId() == R.id.addGroup) {
//            System.out.println("添加群聊");
            inputTitleDialog();
        }
//        else if (v.getId() == R.id.groups) {
//            System.out.println("查看所有群聊");
//            Intent intent = new Intent(groupListActivity, GroupListActivity.class);
//            intent.putExtra("mail",msg);
//            indexActivity.startActivity(intent);
////            try {
////                groupsDialog();
////            } catch (JSONException e) {
////                e.printStackTrace();
////            }
//        }
        if (GroupListActivity.data == null) {
            return;
        }
        for (int i = 0; i < GroupListActivity.data.length(); i++) {
            if (v.getId() == 800 + i) {
                Intent intent = new Intent(groupListActivity, GroupChatActivity.class);
//                System.out.println("v.getId():" +v.getId());
                intent.putExtra("id",String.valueOf(v.getId()));
                intent.putExtra("mail",msg);
                try {
                    intent.putExtra("groupId",GroupListActivity.data.getJSONObject(i).get("groupId").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                groupListActivity.startActivity(intent);
            }
        }

    }


    private void inputTitleDialog() {

        final EditText inputServer = new EditText(groupListActivity);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(groupListActivity);
        builder.setTitle("加入群聊").setView(inputServer);
        builder.setPositiveButton("确定",
                (dialog, which) -> {
                    inputName = inputServer.getText().toString();
//                    System.out.println(inputName);
                    new Thread() {
                        @Override
                        public void run(){
                            try {
                                Looper.prepare();
                                Connect();
                                Looper.loop();
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                });
        builder.show();
    }

//    private void groupsDialog() throws JSONException {
//
//        IndexListener indexListener = this;
//        new Thread() {
//            @Override
//            public void run(){
//                try {
//                    Looper.prepare();
//                    indexListener.data = getGroups();
//                    Looper.loop();
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();

//        ScrollView scrollView = new ScrollView(indexActivity);
//        final EditText inputServer = new EditText(indexActivity);
//        inputServer.setFocusable(true);
//        FrameLayout frameLayout = new FrameLayout(indexActivity);
//        scrollView.addView(frameLayout);
//        for (int i = 0; i < indexListener.data.length(); i++) {
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.topMargin = 10+i*5;
//            TextView tv = new TextView(indexActivity);
//            tv.setText(data.getJSONObject(i).get("groupId").toString());
//            frameLayout.addView(tv,layoutParams);
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(indexActivity);
//        builder.setTitle("群聊列表").setView(scrollView);
//        builder.setPositiveButton("确定",
//                (dialog, which) -> {
//                });
//        builder.show();
//    }

    public JSONArray getGroups() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8081/group/getGroup";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject data = new JSONObject();
        data.put("groupId", inputName);
        data.put("mail", msg);
        String content = data.toString();
//        System.out.println("send:" +data);
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
//        System.out.println(result.get("data"));
//        if (result.get("msg").toString().equals("已经加入该群聊")) {
//            new AlertDialog.Builder(indexActivity)
//                    .setTitle("提示")
//                    .setMessage("已经加入该群聊")
//                    .setPositiveButton("确定", null)
//                    .show();
//            return null;
//        } else if (result.get("msg").toString().equals("查不到该群聊")) {
//            new AlertDialog.Builder(indexActivity)
//                    .setTitle("提示")
//                    .setMessage("查不到该群聊")
//                    .setPositiveButton("确定", null)
//                    .show();
//            return null;
//        }
        return (JSONArray) result.get("data");
    }

    public void Connect() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8088/group/addGroup";
//        String urlPath = "http://124.222.180.235:8088/group/addGroup";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject data = new JSONObject();
        data.put("groupId", inputName);
        data.put("mail", msg);
        String content = data.toString();
//        System.out.println("send:" +data);
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
//        System.out.println(result.get("msg"));
        if (result.get("msg").toString().equals("已经加入该群聊")) {
            new AlertDialog.Builder(groupListActivity)
                    .setTitle("提示")
                    .setMessage("已经加入该群聊")
                    .setPositiveButton("确定", null)
                    .show();
        } else if (result.get("msg").toString().equals("查不到该群聊")) {
            new AlertDialog.Builder(groupListActivity)
                    .setTitle("提示")
                    .setMessage("查不到该群聊")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
}

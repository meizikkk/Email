package com.lyc.Email.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lyc.Email.R;
import com.lyc.Email.listener.GroupChatListener;
import com.lyc.Email.listener.GroupListListener;
import com.lyc.Email.listener.IndexListener;
import com.lyc.Email.thread.GroupListThread;
import com.lyc.Email.thread.IndexThread;
import com.lyc.Email.utils.CustomUserAvatar;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class GroupListActivity extends AppCompatActivity {
    private List<Object> msgs = new ArrayList<>();

    private String msg;

    public static JSONArray data = new JSONArray();

    public static Boolean flag = false;

    private final GroupListActivity groupListActivity = this;


    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        Intent intent = getIntent();
        msg = intent.getStringExtra("email");
//        IndexActivity indexActivity = this;

        ScrollView scrollView = groupListActivity.findViewById(R.id.scrollView);
        FrameLayout frameLayout = groupListActivity.findViewById(R.id.scrollFrame);


        new Thread() {
            @Override
            public void run() {
                try {
                    data = Connect();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        GroupListListener gl = new GroupListListener(this, msg, frameLayout);
        findViewById(R.id.sendMail).setOnClickListener(gl);
        findViewById(R.id.flush).setOnClickListener(gl);
        findViewById(R.id.addGroup).setOnClickListener(gl);
//        findViewById(R.id.groups).setOnClickListener(gl);

        GroupListThread groupListThread = new GroupListThread(this, msg, frameLayout, gl);
        groupListThread.start();
        TextView mail = groupListActivity.findViewById(R.id.mail);
        mail.setText(msg.split("@")[0]);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (data == null) {
                        return;
                    }
                    makeAvatars(data, groupListActivity, frameLayout);
                    makeMailFrom(data, groupListActivity, frameLayout, gl);
                    makeLines(data, groupListActivity, frameLayout);
//                    makeTitles(data, groupListActivity, frameLayout);
//                    makeContent(data, groupListActivity, frameLayout);
//                    makeTimeTop(data, groupListActivity, frameLayout);
//                    makeTimeBottom(data, groupListActivity, frameLayout);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public JSONArray getGroups() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8088/group/getGroup";
//        String urlPath = "http://124.222.180.235:8088/group/getGroup";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject data = new JSONObject();
//        data.put("groupId", inputName);
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
        if (result.get("msg").toString().equals("你还没有加入任何群聊")) {
            return null;
        }
        return (JSONArray) result.get("data");
    }


    public JSONArray Connect() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8088/group/getGroup";
//        String urlPath = "http://124.222.180.235:8088/group/getGroup";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("mail", msg);
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
//        System.out.println(result.get("data"));
        if (result.get("data").toString().equals("null")) {
            return null;
        }
        return (JSONArray) result.get("data");
    }

    /**
     * 生成头像
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeAvatars(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) throws JSONException {
        // 头像布局参数
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        avatarLayout.leftMargin = 70;
        avatarLayout.height = 130;
        avatarLayout.width = 130;

        for (int i = 0; i < data.length(); i++) {
//            XCRoundImageView iv= new XCRoundImageView(indexActivity);
//            iv.setLayoutParams(layout);
//            iv.setImageResource(R.drawable.avatar);

            CustomUserAvatar ca = new CustomUserAvatar(groupListActivity);
            ca.setUserName("G");
//            ca.setBackgroundColor(Color.BLACK);
            avatarLayout.topMargin = frameLayout.getTop() + i * 210 + 20;
            frameLayout.addView(ca, avatarLayout);
//            indexActivity.addContentView(ca,layout);

        }
    }

    /**
     * 生成发送人id
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeMailFrom(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout, GroupListListener gl) throws JSONException {
        // 发送方布局参数
        LinearLayout.LayoutParams emailFromLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emailFromLayout.leftMargin = 300;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupListActivity);
            mTv.setId(800 + i);
            mTv.setLayoutParams(emailFromLayout);
            mTv.setTextColor(groupListActivity.getResources().getColor(R.color.black));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("groupId").toString());
//            System.out.println(frameLayout.getY());
            emailFromLayout.topMargin = frameLayout.getTop() + i * 210 + 30;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, emailFromLayout);
            mTv.setOnClickListener(gl);
        }
    }

    /**
     * 生成标题
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeTitles(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) throws JSONException {
        // 标题布局参数
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLayout.leftMargin = 300;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupListActivity);
            mTv.setLayoutParams(titleLayout);
//            mTv.setTextColor(indexActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("title").toString());
            titleLayout.topMargin = frameLayout.getTop() + i * 210 + 80;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, titleLayout);

        }
    }

    /**
     * 生成内容
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeContent(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) throws JSONException {
        // 内容布局参数
        LinearLayout.LayoutParams contentLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentLayout.leftMargin = 300;
        contentLayout.width = 720;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupListActivity);
            mTv.setLayoutParams(contentLayout);
            mTv.setTextColor(groupListActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            mTv.setEllipsize(TextUtils.TruncateAt.END);
            mTv.setMaxLines(1);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("content").toString());
            contentLayout.topMargin = frameLayout.getTop() + i * 210 + 130;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, contentLayout);

        }
    }

    /**
     * 生成发送时间上半部分
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeTimeTop(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) throws JSONException {
        // 时间布局参数
        LinearLayout.LayoutParams topTimeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topTimeLayout.leftMargin = 840;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupListActivity);

            mTv.setLayoutParams(topTimeLayout);

            mTv.setTextColor(groupListActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("sendTime").toString().split("T")[0]);

            topTimeLayout.topMargin = frameLayout.getTop() + i * 210 + 10;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, topTimeLayout);

        }
    }

    /**
     * 生成发送时间下半部分
     *
     * @param data
     * @param groupListActivity
     * @throws JSONException
     */
    public void makeTimeBottom(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) throws JSONException {

        LinearLayout.LayoutParams bottomTimeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomTimeLayout.leftMargin = 880;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupListActivity);

            mTv.setLayoutParams(bottomTimeLayout);

            mTv.setTextColor(groupListActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("sendTime").toString().split("T")[1]);

            bottomTimeLayout.topMargin = frameLayout.getTop() + i * 210 + 45;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, bottomTimeLayout);

        }
    }

    public void makeLines(JSONArray data, GroupListActivity groupListActivity, FrameLayout frameLayout) {
        // 分割线布局参数
        LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lineLayout.leftMargin = 90;
        lineLayout.width = 950;

        for (int i = 0; i < data.length() - 1; i++) {
            TextView mTv = new TextView(groupListActivity);

            mTv.setLayoutParams(lineLayout);
            mTv.setBackgroundColor(Color.LTGRAY);
            mTv.setHeight(2);


            lineLayout.topMargin = frameLayout.getTop() + i * 210 + 200;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, lineLayout);

        }
    }
}

package com.lyc.Email.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.lyc.Email.R;
import com.lyc.Email.listener.GroupChatListener;
import com.lyc.Email.listener.GroupListListener;
import com.lyc.Email.thread.GroupListThread;
import com.lyc.Email.thread.GroupMsgThread;
import com.lyc.Email.utils.CustomUserAvatar;
import com.lyc.Email.utils.TextViewLinesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class GroupChatActivity extends AppCompatActivity {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);


    private List<Object> msgs = new ArrayList<>();

    private String id;

    private String mail;

    private String groupId;

    public static JSONArray data = new JSONArray();

    public static Boolean flag = false;

    private final GroupChatActivity groupChatActivity = this;

    private GroupMsgThread groupMsgThread;

    private Timer timer = new Timer();

    private int height = 0;

    private static List<Integer> lines = new ArrayList<>();

    private static int screenHeight = 0;

    private static int avatarHeight = 0;

    private static int senderHeight = 0;

    private static int topTimeHeight = 0;

    private static int bottomTimeHeight = 0;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //126 165 208 251
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        TextView groupName = findViewById(R.id.GroupName);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        mail = intent.getStringExtra("mail");
        groupId = intent.getStringExtra("groupId");
        try {
            groupName.setText(GroupListActivity.data.getJSONObject(Integer.parseInt(id) - 800).get("groupId").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupChatListener gcl = new GroupChatListener(this, mail, Integer.parseInt(groupId));
        findViewById(R.id.sendMsg).setOnClickListener(gcl);
//        findViewById(R.id.group_send).setOnClickListener(gcl);
        findViewById(R.id.group_send).bringToFront();
        findViewById(R.id.upLoadImage).setOnClickListener(gcl);

//        ScrollView scrollView = groupListActivity.findViewById(R.id.scrollView);
        FrameLayout frameLayout = groupChatActivity.findViewById(R.id.scrollFrame);


        new Thread() {
            @Override
            public void run() {
                try {
                    data = getGroupMsg();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        GroupChatListener gl = new GroupChatListener(this, mail, Integer.parseInt(id));
//        findViewById(R.id.sendMail).setOnClickListener(gl);
//        findViewById(R.id.flush).setOnClickListener(gl);
//        findViewById(R.id.addGroup).setOnClickListener(gl);
//        findViewById(R.id.groups).setOnClickListener(gl);

        groupMsgThread = new GroupMsgThread(this, id, frameLayout, gl);
        GroupMsgThread.PAUSE = 0;
//        executorService.submit(groupMsgThread);
        groupMsgThread.start();

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                try {
                    if (data == null) {
                        return;
                    }
                    makeAvatars(data, groupChatActivity, frameLayout);
                    makeMailFrom(data, groupChatActivity, frameLayout, gl);
//                    makeLines(data, groupChatActivity, frameLayout);
//                    makeTitles(data, groupListActivity, frameLayout);
                    makeContent(data, groupChatActivity, frameLayout);
                    makeTimeTop(data, groupChatActivity, frameLayout);
                    makeTimeBottom(data, groupChatActivity, frameLayout);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
//        groupMsgThread.interrupt();
        GroupMsgThread.PAUSE = 1;
        super.onDestroy();
    }

    public JSONArray getGroupMsg() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8088/group/getGroupMsg";
//        String urlPath = "http://124.222.180.235:8088/group/getGroupMsg";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject data = new JSONObject();
        data.put("groupId", groupId);
        String content = data.toString();
//        System.out.println("send:" + data);
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
        if (result.get("msg").toString().equals("暂时没有信息噢")) {
            return null;
        }
        return (JSONArray) result.get("data");
    }

    public JSONArray Connect() throws IOException, JSONException {
        String urlPath = "http://192.168.43.48:8088/group/getGroupMsg";
//        String urlPath = "http://124.222.180.235:8088/group/getGroupMsg";
//        System.out.println("请求");
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject data = new JSONObject();
        data.put("groupId", groupId);
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
     * @param groupChatActivity
     * @throws JSONException
     */
    public void makeAvatars(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout) throws JSONException {
        // 头像布局参数
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        avatarLayout.leftMargin = 40;
        avatarLayout.height = 120;
        avatarLayout.width = 120;

        for (int i = 0; i < data.length(); i++) {
//            XCRoundImageView iv= new XCRoundImageView(indexActivity);
//            iv.setLayoutParams(layout);
//            iv.setImageResource(R.drawable.avatar);

            CustomUserAvatar ca = new CustomUserAvatar(groupChatActivity);
            ca.setUserName(data.getJSONObject(i).get("messageFrom").toString());
//            ca.setBackgroundColor(Color.BLACK);
            if (lines.size() == 0) {
                frameLayout.addView(ca, avatarLayout);
                continue;
            }
            if (i == 0) {
                avatarLayout.topMargin = frameLayout.getTop() + 25;
                avatarHeight = avatarLayout.topMargin;
                frameLayout.addView(ca, avatarLayout);
                continue;
            }
            avatarLayout.topMargin = avatarHeight + 70 + lines.get(i - 1);
            avatarHeight = avatarLayout.topMargin;
            frameLayout.addView(ca, avatarLayout);
//            indexActivity.addContentView(ca,layout);

        }
        avatarHeight = 0;
    }

    /**
     * 生成发送人id
     *
     * @param data
     * @param groupChatActivity
     * @throws JSONException
     */
    public void makeMailFrom(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout, GroupChatListener gl) throws JSONException {
        // 发送方布局参数
        LinearLayout.LayoutParams emailFromLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emailFromLayout.leftMargin = 210;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupChatActivity);
            mTv.setId(800 + i);
            mTv.setLayoutParams(emailFromLayout);
            mTv.setTextColor(groupChatActivity.getResources().getColor(R.color.black));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
//            System.out.println("messageFrom:"+ data.getJSONObject(i).get("messageFrom"));
            mTv.setText(data.getJSONObject(i).get("messageFrom").toString());
            if (lines.size() == 0) {
                frameLayout.addView(mTv, emailFromLayout);
                continue;
            }
            if (i == 0) {
                emailFromLayout.topMargin = frameLayout.getTop() + 25;
                frameLayout.addView(mTv, emailFromLayout);
                senderHeight = emailFromLayout.topMargin;
                continue;
            }
//            System.out.println(frameLayout.getY());
            emailFromLayout.topMargin = senderHeight + 70 + lines.get(i - 1);
            senderHeight = emailFromLayout.topMargin;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, emailFromLayout);
            mTv.setOnClickListener(gl);
        }
        senderHeight = 0;
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
     * @param groupChatActivity
     * @throws JSONException
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("UseCompatLoadingForDrawables")
    public void makeContent(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout) throws JSONException {
        // 内容布局参数
        LinearLayout.LayoutParams contentLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentLayout.leftMargin = 160;
        contentLayout.rightMargin = 60;
        contentLayout.gravity = Gravity.CENTER;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupChatActivity);
            mTv.setId(200 + i);
            mTv.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
            mTv.setBackground(getDrawable(R.drawable.chat));
            mTv.setLayoutParams(contentLayout);
            mTv.setTextColor(Color.BLACK);
            mTv.setText(data.getJSONObject(i).get("sendContent").toString());
//            System.out.println("第"+(i+1)+"个："+ height);
            mTv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    height = mTv.getHeight();
                    if (lines.size() < data.length()) {
                        lines.add(height);
                    }
//                    System.out.println(lines);
//                    System.out.println(height);
                    mTv.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            });
            if (lines.size() == 0) {
                frameLayout.addView(mTv, contentLayout);
                continue;
            }
            if (i == 0) {
                contentLayout.topMargin = frameLayout.getTop() + 85;
                screenHeight = contentLayout.topMargin;
                frameLayout.addView(mTv, contentLayout);
                continue;
            }
            contentLayout.topMargin = screenHeight + lines.get(i - 1) + 70;
            screenHeight = contentLayout.topMargin;
            frameLayout.addView(mTv, contentLayout);
//            System.out.println(lines);
        }
        screenHeight = 0;
    }

    /**
     * 生成发送时间上半部分
     *
     * @param data
     * @param groupChatActivity
     * @throws JSONException
     */
    public void makeTimeTop(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout) throws JSONException {
        // 时间布局参数
        LinearLayout.LayoutParams topTimeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topTimeLayout.leftMargin = 860;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupChatActivity);

            mTv.setLayoutParams(topTimeLayout);

            mTv.setTextColor(groupChatActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("sendTime").toString().split("T")[0]);
            if (lines.size() == 0) {
                frameLayout.addView(mTv, topTimeLayout);
                continue;
            }
            if (i == 0) {
                topTimeLayout.topMargin = frameLayout.getTop() + 25;
                frameLayout.addView(mTv, topTimeLayout);
                topTimeHeight = topTimeLayout.topMargin;
                continue;
            }
            topTimeLayout.topMargin = topTimeHeight + 70 + lines.get(i - 1);
            topTimeHeight = topTimeLayout.topMargin;
//            indexActivity.addContentView(mTv, layout);
            frameLayout.addView(mTv, topTimeLayout);

        }
        topTimeHeight = 0;
    }


    /**
     * 生成发送时间下半部分
     *
     * @param data
     * @param groupChatActivity
     * @throws JSONException
     */
    public void makeTimeBottom(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout) throws JSONException {

        LinearLayout.LayoutParams bottomTimeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomTimeLayout.leftMargin = 900;

        for (int i = 0; i < data.length(); i++) {
            TextView mTv = new TextView(groupChatActivity);

            mTv.setLayoutParams(bottomTimeLayout);

            mTv.setTextColor(groupChatActivity.getResources().getColor(R.color.lightGray));
            mTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            //字体加粗
//            mTv.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
//            mTv.getPaint().setAntiAlias(true);//抗锯齿
            mTv.setText(data.getJSONObject(i).get("sendTime").toString().split("T")[1]);
            if (lines.size() == 0) {
                frameLayout.addView(mTv, bottomTimeLayout);
                continue;
            }
            if (i == 0) {
                bottomTimeLayout.topMargin = frameLayout.getTop() + 55;
                frameLayout.addView(mTv, bottomTimeLayout);
                bottomTimeHeight = bottomTimeLayout.topMargin;
                continue;
            }
            bottomTimeLayout.topMargin = bottomTimeHeight + 70 + lines.get(i - 1);
//            indexActivity.addContentView(mTv, layout);
            bottomTimeHeight = bottomTimeLayout.topMargin;
            frameLayout.addView(mTv, bottomTimeLayout);

        }
        bottomTimeHeight = 0;
    }


    public void makeImage(JSONArray data, GroupChatActivity groupChatActivity, FrameLayout frameLayout) {
        // 分割线布局参数
        LinearLayout.LayoutParams lineLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lineLayout.leftMargin = 90;
        lineLayout.width = 950;


        for (int i = 0; i < data.length() - 1; i++) {
            TextView mTv = new TextView(groupChatActivity);

            mTv.setLayoutParams(lineLayout);
            mTv.setBackgroundColor(Color.LTGRAY);
            mTv.setHeight(2);

            lineLayout.topMargin = frameLayout.getTop() + i * 210 + 200;

            frameLayout.addView(mTv, lineLayout);

        }
    }

}

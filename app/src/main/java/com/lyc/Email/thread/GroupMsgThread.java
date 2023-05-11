package com.lyc.Email.thread;

import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;

import com.lyc.Email.R;
import com.lyc.Email.activity.GroupChatActivity;
import com.lyc.Email.activity.GroupListActivity;
import com.lyc.Email.listener.GroupChatListener;
import com.lyc.Email.listener.GroupListListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupMsgThread extends Thread {
    public static int CHANGE = 0;

    public static int PAUSE = 0;

    public static boolean flag = false;

    public static boolean isChange = false;

    private GroupChatActivity groupChatActivity;

    private String msg;

    private FrameLayout frameLayout;

    private Handler handler = new Handler();

    private static int length = 0;

    private GroupChatListener gl;

    private List<Integer> lengths = new ArrayList<>();

    public GroupMsgThread(GroupChatActivity groupChatActivity, String msg, FrameLayout frameLayout, GroupChatListener gl) {
        this.groupChatActivity = groupChatActivity;
        this.msg = msg;
        this.frameLayout = frameLayout;
        this.gl = gl;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void run() {
        while (true) {
            if (PAUSE == 1) {
                break;
            }

            if (GroupChatActivity.flag) {
                groupChatActivity.finish();
                GroupListActivity.flag = false;
            }
            try {
                JSONArray data = groupChatActivity.getGroupMsg();

                if (data == null) {
                    GroupListActivity.data = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                GroupChatActivity.data = data;
                lengths.add(data.length());
                if (lengths.size() > 1) {
                    if (lengths.get(lengths.size() - 1) != lengths.get(lengths.size() - 2)) {
                        isChange = true;
                    }
                }
                groupChatActivity.runOnUiThread(() -> {
                    try {
                        frameLayout.removeAllViews();
                        groupChatActivity.makeAvatars(data, groupChatActivity, frameLayout);
                        groupChatActivity.makeMailFrom(data, groupChatActivity, frameLayout, gl);
                        groupChatActivity.makeContent(data, groupChatActivity, frameLayout);
                        groupChatActivity.makeTimeTop(data, groupChatActivity, frameLayout);
                        groupChatActivity.makeTimeBottom(data, groupChatActivity, frameLayout);
                        final ScrollView mscrollView = (ScrollView) groupChatActivity.findViewById(R.id.scrollView2);
                        if (isChange) {
//                            System.out.println("进入");
                            mscrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            isChange = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

                length = GroupChatActivity.data.length();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            flag = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}

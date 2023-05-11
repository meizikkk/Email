package com.lyc.Email.thread;

import android.os.Handler;
import android.os.Vibrator;
import android.widget.FrameLayout;

import com.lyc.Email.activity.GroupListActivity;
import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.listener.GroupListListener;
import com.lyc.Email.listener.IndexListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class GroupListThread extends Thread {
    public static  int PAUSE = 0;

    public static boolean flag = false;

    private GroupListActivity groupListActivity;

    private String msg;

    private FrameLayout frameLayout;

    private Handler handler = new Handler();

    private int length = 0;

    private GroupListListener gl;

    public GroupListThread(GroupListActivity groupListActivity, String msg, FrameLayout frameLayout, GroupListListener gl) {
        this.groupListActivity = groupListActivity;
        this.msg = msg;
        this.frameLayout = frameLayout;
        this.gl = gl;
    }

    @Override
    public void run() {
        while (true) {

            if (PAUSE == 1) {
                break;
            }

            if (GroupListActivity.flag) {
                groupListActivity.finish();
                GroupListActivity.flag = false;
            }
            try {
                if (groupListActivity.getGroups() == null) {
                    GroupListActivity.data = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
                }
//                System.out.println("IndexActivity.data.length():" + IndexActivity.data.length());
//                System.out.println("length:" + length);

                GroupListActivity.data = groupListActivity.getGroups();
                JSONArray data = groupListActivity.getGroups();

//                if (GroupListActivity.data.length() != length && length != 0) {
//                    System.out.println("变化");
//                    Vibrator vibrator = (Vibrator) groupListActivity.getSystemService(groupListActivity.VIBRATOR_SERVICE);
//                    long[] patter = {0, 200, 230, 200};
//                    vibrator.vibrate(patter, -1);
//                }
                groupListActivity.runOnUiThread(() -> {
                    try {
                        frameLayout.removeAllViews();
                        groupListActivity.makeAvatars(data, groupListActivity, frameLayout);
                        groupListActivity.makeMailFrom(data, groupListActivity, frameLayout, gl);
//                        groupListActivity.makeTitles(data, groupListActivity, frameLayout);
//                        groupListActivity.makeContent(data, groupListActivity, frameLayout);
//                        groupListActivity.makeTimeTop(data, groupListActivity, frameLayout);
//                        groupListActivity.makeTimeBottom(data, groupListActivity, frameLayout);
                        groupListActivity.makeLines(data, groupListActivity, frameLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
//                length = GroupListActivity.data.length();
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

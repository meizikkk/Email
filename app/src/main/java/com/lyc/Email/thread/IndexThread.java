package com.lyc.Email.thread;

import android.os.Handler;
import android.os.Vibrator;
import android.widget.FrameLayout;

import com.lyc.Email.activity.IndexActivity;
import com.lyc.Email.listener.IndexListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class IndexThread extends Thread {

    public static final int CHANGE = 0;

    public static boolean flag = false;

    private IndexActivity indexActivity;

    private String msg;

    private FrameLayout frameLayout;

    private Handler handler = new Handler();

    private int length = 0;

    private IndexListener il;

    public IndexThread(IndexActivity indexActivity, String msg, FrameLayout frameLayout, IndexListener il) {
        this.indexActivity = indexActivity;
        this.msg = msg;
        this.frameLayout = frameLayout;
        this.il = il;
    }

    @Override
    public void run() {
        while (true) {
            if (IndexActivity.flag) {
                indexActivity.finish();
                IndexActivity.flag = false;
            }
            try {
                JSONArray data = indexActivity.Connect();
                if (data == null) {
                    IndexActivity.data = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    continue;
                }
//                System.out.println("IndexActivity.data.length():" + IndexActivity.data.length());
//                System.out.println("length:" + length);

                IndexActivity.data = data;

//                if (IndexActivity.data.length() != length && length != 0) {
//                    System.out.println("变化");
//                    Vibrator vibrator = (Vibrator) indexActivity.getSystemService(indexActivity.VIBRATOR_SERVICE);
//                    long[] patter = {0, 200, 230, 200};
//                    vibrator.vibrate(patter, -1);
//                }
                indexActivity.runOnUiThread(() -> {
                    try {
                        frameLayout.removeAllViews();
                        indexActivity.makeAvatars(data, indexActivity, frameLayout);
                        indexActivity.makeMailFrom(data, indexActivity, frameLayout, il);
                        indexActivity.makeTitles(data, indexActivity, frameLayout);
                        indexActivity.makeContent(data, indexActivity, frameLayout);
                        indexActivity.makeTimeTop(data, indexActivity, frameLayout);
                        indexActivity.makeTimeBottom(data, indexActivity, frameLayout);
                        indexActivity.makeLines(data, indexActivity, frameLayout);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                length = IndexActivity.data.length();
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

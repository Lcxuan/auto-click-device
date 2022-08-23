package com.lcxuan.autoclickdevice.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.lcxuan.autoclickdevice.pojo.Coordinate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyAccessibilityService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LCXUANTAG", "onStartCommand....");

        // 获取所有需要点击的坐标
        List<Coordinate> coordinateList = (List<Coordinate>) intent.getSerializableExtra("coordinateList");
        // 获取点击次数
        Bundle extras = intent.getExtras();
        Integer count = Integer.parseInt(extras.getString("count"));

        Log.e("LCXUANTAG", "Service 坐标对象：" + coordinateList + ", 点击次数：" + count);

        startClick(coordinateList, count);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startClick(List<Coordinate> coordinateList, Integer count){
        // 使用线程池
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    for (Coordinate coordinate : coordinateList) {
                        GestureDescription.Builder builder = new GestureDescription.Builder();
                        Path p = new Path();
                        p.moveTo(coordinate.getX() , coordinate.getY());
                        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 10L));
                        GestureDescription gesture = builder.build();

                        dispatchGesture(gesture, new GestureResultCallback() {
                            @Override
                            public void onCompleted(GestureDescription gestureDescription) {
                                super.onCompleted(gestureDescription);
                                Log.e("LCXUANTAG", "onCompleted: 完成..........");
                            }

                            @Override
                            public void onCancelled(GestureDescription gestureDescription) {
                                super.onCancelled(gestureDescription);
                                Log.e("LCXUANTAG", "onCompleted: 取消..........");
                            }
                        }, null);

                        Log.e("LCXUANTAG", "当前点击坐标X=" + coordinate.getX() + ", y=" + coordinate.getY());

                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // 发送广播，用于告诉Activity任务已经完成，更新界面UI
                Intent intent = new Intent();
                intent.setAction("com.lcxuan.receiver.coordinate");
                sendBroadcast(intent);
            }
        });

        service.shutdown();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {

    }
}

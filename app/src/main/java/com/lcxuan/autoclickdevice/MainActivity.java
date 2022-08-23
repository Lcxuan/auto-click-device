package com.lcxuan.autoclickdevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.lcxuan.autoclickdevice.pojo.Coordinate;
import com.lcxuan.autoclickdevice.service.MyAccessibilityService;
import com.lcxuan.autoclickdevice.util.PermissionCheckUtil;
import com.lcxuan.autoclickdevice.view.FloatingWindowMenu;
import com.lcxuan.autoclickdevice.view.TakeAimView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button test;
    private Button open;
    private EditText countEditText;

    private CoordinateBroadcastReceiver receiver;

    private WindowManager windowManager; // 用于在页面添加悬浮窗
    private List<TakeAimView> takeAimViewList = new ArrayList<>();  // 悬浮窗管理
    private List<WindowManager.LayoutParams> paramList = new ArrayList<>(); // 悬浮窗参数管理
    private List<Coordinate> coordinateList = new ArrayList<>();    // 悬浮窗坐标管理
    private FloatingWindowMenu floatingWindowMenu;  // 悬浮窗菜单
    private WindowManager.LayoutParams mFloatingMenuParams; // 悬浮窗菜单参数

    private Intent intent;

    private int sum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAccessibilityPermission();
        initView();
        addListeners();
        initBroadcastReceiver();
        floatingWindowMenu();
    }


    @Override
    protected void onDestroy() {

        if (intent != null){
            // 关闭Service
            stopService(intent);
        }

        // 将瞄准悬浮窗关闭
        if (takeAimViewList.size() > 0){
            for (TakeAimView takeAimView : takeAimViewList) {
                windowManager.removeView(takeAimView);
            }
        }

        // 将悬浮窗菜单关闭
        windowManager.removeView(floatingWindowMenu);

        // 销毁广播
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    /**
     * 悬浮窗菜单
     */
    private void floatingWindowMenu() {
        mFloatingMenuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                0,
                PixelFormat.TRANSPARENT
        );

        // 获取屏幕高度和宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        int statusHeight = 0;
        int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusHeight = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
        }

        Log.e("LCXUANTAG","屏幕高度：" + height + "，状态栏高度：" + statusHeight);

        mFloatingMenuParams.x = width;
        mFloatingMenuParams.y = (height - statusHeight) / 2;

        mFloatingMenuParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mFloatingMenuParams.gravity = Gravity.LEFT | Gravity.TOP;

        // 设置window类型
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//API Level 26
            mFloatingMenuParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mFloatingMenuParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }

        floatingWindowMenu = new FloatingWindowMenu(this);
        floatingWindowMenu.setOnChangeListener(new FloatingWindowMenu.FloatingMenuInterface() {
            @Override
            public void start() {
                startClick();
            }

            @Override
            public void add() {
                addSuspensionWindow();
            }

            @Override
            public void remove() {
                removeSuspensionWindow();
            }

            @Override
            public void close() {
                finishApp();
            }
        });

        floatingWindowMenu.setOnTouchListener(new FloatingWindowOnTouchListener());

        windowManager.addView(floatingWindowMenu, mFloatingMenuParams);
    }

    /**
     * 关闭连点器
     */
    private void finishApp(){
        PackageManager manager = getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage("com.lcxuan.autoclickdevice");
        startActivity(intent);

        new AlertDialog.Builder(this)
                .setTitle("是否关闭连点器")
                .setMessage("请谨慎操作，如果当前连点器在运行请不要关闭！！！")
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * 初始化广播
     */
    private void initBroadcastReceiver() {
        // 实例化广播对象
        receiver = new CoordinateBroadcastReceiver();

        // 实例化IntentFilter，并设置广播类型
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.lcxuan.receiver.coordinate");

        // 注册广播
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.test:
                test();
                break;
            case R.id.open:
                requestSuspensionWindowPermission();
                break;
        }
    }

    /**
     * 测试
     */
    private void test() {
        Log.e("LCXUANTAG","被点击。。。。");
        Toast.makeText(this, "被点击", Toast.LENGTH_SHORT).show();
    }

    /**
     * 移除悬浮窗
     */
    private void removeSuspensionWindow() {
        // 移除所有悬浮窗
        if (takeAimViewList.size() > 0){
            for (TakeAimView takeAimView : takeAimViewList) {
                windowManager.removeView(takeAimView);
            }
        }

        // 清空悬浮窗集合、悬浮窗参数集合和坐标集合
        takeAimViewList.clear();
        paramList.clear();
        coordinateList.clear();

        sum = 0;

        Log.e("LCXUANTAG","remove takeAimViewList：" + takeAimViewList);
        Log.e("LCXUANTAG","remove paramList：" + paramList);
        Log.e("LCXUANTAG","remove coordinateList：" + coordinateList);
    }

    /**
     * 开始点击
     */
    private void startClick() {
        if (takeAimViewList.size() <= 0){
            Toast.makeText(this, "请先选择需要点击的位置！！！", Toast.LENGTH_SHORT).show();
            return;
        }

        String countVal = countEditText.getText().toString();

        Log.e("LCXUANTAG", "所有坐标对象：" + coordinateList);
        intent = new Intent(this, MyAccessibilityService.class);
        intent.putExtra("coordinateList", (Serializable) coordinateList);

        countVal = countVal.equals("") ? 100 + "" : countVal;
        intent.putExtra("count", countVal);

        for (TakeAimView takeAimView : takeAimViewList) {
            takeAimView.setVisibility(View.INVISIBLE);
        }

        startService(intent);
    }

    /**
     * 添加悬浮窗
     */
    private void addSuspensionWindow() {
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                0,
                PixelFormat.TRANSPARENT
        );

        mParams.x = 0;
        mParams.y = 0;

        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mParams.gravity = Gravity.LEFT | Gravity.TOP;

        // 设置window类型
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){//API Level 26
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }

        TakeAimView takeAimView = new TakeAimView(this)
                .setTakeAimSize(150, 150)
                .setNumId(++sum)
                .setTakeAimStyle(R.drawable.ic_take_aim_grey);

        takeAimView.setOnTouchListener(new FloatingOnTouchListener());

        takeAimViewList.add(takeAimView);
        paramList.add(mParams);
        coordinateList.add(new Coordinate(sum, 0, 0));

        windowManager.addView(takeAimView, mParams);
    }

    /**
     * 开启悬浮窗权限
     */
    private void requestSuspensionWindowPermission() {
        if (!PermissionCheckUtil.isSuspensionWindow(getApplicationContext())){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+getPackageName()));
            startActivityForResult(intent,100);
        }
    }

    /**
     * 开启无障碍服务权限
     */
    private void requestAccessibilityPermission() {
        if (!PermissionCheckUtil.isAccessibility(this, MyAccessibilityService.class.getName())) {
            Toast.makeText(this, "请求打开无障碍服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * 添加事件
     */
    private void addListeners() {
        countEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start > 0) {
                    Integer num = Integer.parseInt(s.toString());
                    if (num > 1000){
                        Toast.makeText(MainActivity.this, "点击的次数不能大于1000次", Toast.LENGTH_SHORT).show();
                        countEditText.setText(1000 + "");
                        countEditText.setSelection(countEditText.length());
                        return;
                    }
                    if (num < 0){
                        Toast.makeText(MainActivity.this, "点击的次数必须大于0次", Toast.LENGTH_SHORT).show();
                        countEditText.setText(1 + "");
                        countEditText.setSelection(countEditText.length());
                        return;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        test.setOnClickListener(this);
        open.setOnClickListener(this);
    }

    /**
     * 初始化View
     */
    private void initView() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        test = findViewById(R.id.test);
        open = findViewById(R.id.open);
        countEditText = findViewById(R.id.count);
    }

    // 悬浮窗菜单移动
    private class FloatingWindowOnTouchListener implements View.OnTouchListener {

        private int x;
        private int y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:

                    x = (int) event.getRawX();
                    y = (int) event.getRawY();

                    break;
                case MotionEvent.ACTION_MOVE:

                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;

                    mFloatingMenuParams.x = mFloatingMenuParams.x + movedX;
                    mFloatingMenuParams.y = mFloatingMenuParams.y + movedY;

                    windowManager.updateViewLayout(floatingWindowMenu, mFloatingMenuParams);

                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }

            return false;
        }
    }

    /**
     * 悬浮窗移动
     */
    private class FloatingOnTouchListener implements View.OnTouchListener {

        private int x;
        private int y;
        private TakeAimView takeAimView;
        private Integer numId;
        private WindowManager.LayoutParams mParams;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x = (int) motionEvent.getRawX();
                    y = (int) motionEvent.getRawY();

                    takeAimView = (TakeAimView) view;
                    numId = takeAimView.getNumId();
                    mParams = paramList.get(numId - 1);

                    Log.e("LCXUANTAG", "当前按下：" + numId);

                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) motionEvent.getRawX();
                    int nowY = (int) motionEvent.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;

                    mParams.x = mParams.x + movedX;
                    mParams.y = mParams.y + movedY;

                    windowManager.updateViewLayout(takeAimView, mParams);

                    break;
                case MotionEvent.ACTION_UP:
                    Coordinate coordinate = new Coordinate(numId, (int) motionEvent.getRawX(), (int) motionEvent.getRawY());
                    coordinateList.set(numId - 1, coordinate);

                    Log.e("LCXUANTAG", "抬起，Coordinate对象：" + coordinate);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 坐标广播
     */
    class CoordinateBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            // UI线程
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("LCXUANTAG", "CoordinateBroadcastReceiver：任务执行完成");
                    Toast.makeText(context, "任务执行完成！！！", Toast.LENGTH_SHORT).show();
                    for (TakeAimView takeAimView : takeAimViewList) {
                        takeAimView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

}
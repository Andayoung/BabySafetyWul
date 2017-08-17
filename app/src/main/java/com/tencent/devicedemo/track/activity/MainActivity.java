package com.tencent.devicedemo.track.activity;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.tencent.devicedemo.R;
import com.tencent.devicedemo.track.TrackApplication;
import com.tencent.devicedemo.track.utils.BitmapUtil;
import com.tencent.devicedemo.track.utils.CommonUtil;
import com.tencent.devicedemo.track.utils.SerialNumberHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private TrackApplication trackApp;
    private TrackQueryFragment trackQueryFragment;
    private FenceFragment fenceFragment;
    private ImageView btnTrace;
    private ImageView btnFence;
    private TextView txtTitle;
    private TextView txtExit;
    private SerialNumberHelper serialNumberHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trackApp = (TrackApplication) getApplicationContext();
        init();
        BitmapUtil.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (serialNumberHelper == null) {
            serialNumberHelper = new SerialNumberHelper(getApplicationContext());
        }
        String serialNumber = serialNumberHelper.read4File();
        if (serialNumber == null || serialNumber.equals("")) {
            Intent intent = new Intent(this, LogOrRegActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            String[] s = serialNumber.split(" ");
            trackApp.entityName = s[0];
        }
    }

    private void init() {
        btnFence = (ImageView) findViewById(R.id.btn_fence);
        btnTrace = (ImageView) findViewById(R.id.btn_trace);
        txtTitle = (TextView) findViewById(R.id.tv_activity_title);
        txtExit=(TextView)findViewById(R.id.exit);
        btnFence.setOnClickListener(this);
        btnTrace.setOnClickListener(this);
        txtExit.setOnClickListener(this);
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        fenceFragment = new FenceFragment();
        transaction.replace(R.id.fry_content, fenceFragment);
        transaction.commit();
        btnFence.setSelected(true);
        btnTrace.setSelected(false);
        txtTitle.setText(R.string.app_fence);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();
        switch (v.getId()) {
            case R.id.btn_fence:
                Log.e("666", "btn_fence");
                if (fenceFragment == null) {
                    fenceFragment = new FenceFragment();
                }
                transaction.replace(R.id.fry_content, fenceFragment);
                btnFence.setSelected(true);
                btnTrace.setSelected(false);
                txtTitle.setText(R.string.app_fence);
                break;
            case R.id.btn_trace:
                Log.e("666", "btn_trace");
                if (trackQueryFragment == null) {
                    trackQueryFragment = new TrackQueryFragment();
                }
                transaction.replace(R.id.fry_content, trackQueryFragment);
                btnFence.setSelected(false);
                btnTrace.setSelected(true);
                txtTitle.setText(R.string.app_trace);
                break;
            case R.id.exit:
                if (serialNumberHelper == null) {
                    serialNumberHelper = new SerialNumberHelper(getApplicationContext());
                }
                serialNumberHelper.deleteFile();
                finish();
                break;
            }

        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }


    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtil.saveCurrentLocation(trackApp);
        if (trackApp.trackConf.contains("is_trace_started")
                && trackApp.trackConf.getBoolean("is_trace_started", true)) {
            // 退出app停止轨迹服务时，不再接收回调，将OnTraceListener置空
            trackApp.mClient.setOnTraceListener(null);
            trackApp.mClient.stopTrace(trackApp.mTrace, null);
        } else {
            trackApp.mClient.clear();
        }
        trackApp.isTraceStarted = false;
        trackApp.isGatherStarted = false;
        SharedPreferences.Editor editor = trackApp.trackConf.edit();
        editor.remove("is_trace_started");
        editor.remove("is_gather_started");
        editor.apply();
        BitmapUtil.clear();
    }
}

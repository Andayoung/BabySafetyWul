package com.tencent.devicedemo.track;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.ProcessOption;
import com.tencent.devicedemo.track.activity.LogOrRegActivity;
import com.tencent.devicedemo.track.utils.CommonUtil;
import com.tencent.devicedemo.track.utils.SerialNumberHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by baidu on 17/1/12.
 */

public class TrackApplication extends Application {
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    private LocRequest locRequest = null;
    public Context mContext = null;
    public SharedPreferences trackConf = null;
    public LBSTraceClient mClient = null;
    public Trace mTrace = null;
    public long serviceId = 145294;
    public String entityName = "UUWatchTrace";
    public boolean isTraceStarted = false;
    public boolean isGatherStarted = false;
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    private SerialNumberHelper serialNumberHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        if (serialNumberHelper == null) {
            serialNumberHelper = new SerialNumberHelper(getApplicationContext());
        }
        String serialNumber=serialNumberHelper.read4File();
        if (serialNumber==null|| serialNumber.equals("")) {
            entityName = "20180101uu000001";
        } else {
            String[] s = serialNumber.split(" ");
            if(s.length<6){
                entityName = "20180101uu000001";
            }else {
                entityName = s[0];
            }
        }
//        entityName = "20180101uu000001";
        // 若为创建独立进程，则不初始化成员变量
        if ("com.baidu.track:remote".equals(CommonUtil.getCurProcessName(mContext))) {
            return;
        }

        SDKInitializer.initialize(mContext);
        initView();
        mClient = new LBSTraceClient(mContext);
        mTrace = new Trace(serviceId, entityName);

        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);
        locRequest = new LocRequest(serviceId);

        mClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }
        });

        clearTraceStatus();
    }

    /**
     * 获取当前位置
     */
    public void getCurrentLocation(OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位

        LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
        ProcessOption processOption = new ProcessOption();
        processOption.setNeedDenoise(true);
        processOption.setRadiusThreshold(100);
        request.setProcessOption(processOption);
        mClient.queryLatestPoint(request, trackListener);
         /*else {
            mClient.queryRealTimeLoc(locRequest, entityListener);
        }*/
    }

    private void initView() {
        getScreenSize();
    }


    /**
     * 获取屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    /**
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     * <p>
     * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
     */
    private void clearTraceStatus() {
        if (trackConf.contains("is_trace_started") || trackConf.contains("is_gather_started")) {
            SharedPreferences.Editor editor = trackConf.edit();
            editor.remove("is_trace_started");
            editor.remove("is_gather_started");
            editor.apply();
        }
    }

    /**
     * 初始化请求公共参数
     *
     * @param request
     */
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }


}

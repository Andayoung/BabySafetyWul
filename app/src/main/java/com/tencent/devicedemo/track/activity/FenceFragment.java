package com.tencent.devicedemo.track.activity;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.trace.api.fence.CircleFence;
import com.baidu.trace.api.fence.CreateFenceRequest;
import com.baidu.trace.api.fence.CreateFenceResponse;
import com.baidu.trace.api.fence.DeleteFenceRequest;
import com.baidu.trace.api.fence.DeleteFenceResponse;
import com.baidu.trace.api.fence.FenceAlarmInfo;
import com.baidu.trace.api.fence.FenceInfo;
import com.baidu.trace.api.fence.FenceListRequest;
import com.baidu.trace.api.fence.FenceListResponse;
import com.baidu.trace.api.fence.FenceShape;
import com.baidu.trace.api.fence.FenceType;
import com.baidu.trace.api.fence.HistoryAlarmRequest;
import com.baidu.trace.api.fence.HistoryAlarmResponse;
import com.baidu.trace.api.fence.MonitoredStatusByLocationResponse;
import com.baidu.trace.api.fence.MonitoredStatusResponse;
import com.baidu.trace.api.fence.OnFenceListener;
import com.baidu.trace.api.fence.PolygonFence;
import com.baidu.trace.api.fence.PolylineFence;
import com.baidu.trace.api.fence.UpdateFenceResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.LatLng;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.tencent.devicedemo.R;
import com.tencent.devicedemo.clusterutil.clustering.Cluster;
import com.tencent.devicedemo.clusterutil.clustering.ClusterManager;
import com.tencent.devicedemo.track.TrackApplication;
import com.tencent.devicedemo.track.dialog.FenceCreateDialog;
import com.tencent.devicedemo.track.dialog.FenceHistoryDialog;
import com.tencent.devicedemo.track.dialog.FenceOperateDialog;
import com.tencent.devicedemo.track.dialog.FenceSettingDialog;
import com.tencent.devicedemo.track.model.CurrentLocation;
import com.tencent.devicedemo.track.utils.BitmapUtil;
import com.tencent.devicedemo.track.utils.CommonUtil;
import com.tencent.devicedemo.track.utils.Constants;
import com.tencent.devicedemo.track.utils.MapUtil;
import com.tencent.devicedemo.track.utils.MyItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/26 0026.
 */

public class FenceFragment extends Fragment implements View.OnClickListener,
        ClusterManager.OnClusterItemClickListener<MyItem>,
        ClusterManager.OnClusterClickListener<MyItem>,
        BaiduMap.OnMapClickListener, BaiduMap.OnMapLoadedCallback {

    private TrackApplication trackApp = null;
    private ClusterManager<MyItem> mClusterManager = null;
    private FenceType fenceType = FenceType.server;
    private FenceShape fenceShape = FenceShape.circle;
    private String fenceName = null;
    private OnFenceListener fenceListener = null;
    private FenceSettingDialog fenceSettingDialog = null;
    private FenceSettingDialog.Callback settingCallback = null;
    private FenceCreateDialog fenceCreateDialog = null;
    private FenceCreateDialog.Callback createCallback = null;
    private FenceOperateDialog fenceOperateDialog = null;
    private FenceHistoryDialog fenceHistoryDialog = null;
    private MapUtil mapUtil = null;
    private Map<String, Overlay> overlays = new HashMap<>();
    private long beginTime = 0;
    private long endTime = 0;
    private com.baidu.mapapi.model.LatLng circleCenter = null;
    private List<LatLng> traceVertexes = new ArrayList<>();
    private List<com.baidu.mapapi.model.LatLng> mapVertexes = new ArrayList<>();
    private int vertexesNumber = 3;
    private double radius = 1000;
    private int denoise = 0;
    private int offset = 200;
    private Map<Integer, com.baidu.mapapi.model.LatLng> tempLatLngs = new HashMap<>();
    private Map<Integer, Overlay> tempOverlays = new HashMap<>();
    private List<Overlay> tempMarks = new ArrayList<>();
    private String fenceKey;
    private int vertexIndex = 0;
    private RealTimeHandler realTimeHandler = new RealTimeHandler();
    private RealTimeLocRunnable realTimeLocRunnable = null;
    private View convertView;
    private ImageView imgSet;
    private OnTrackListener trackListener = null;
    private OnTraceListener traceListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trackApp = (TrackApplication) getActivity().getApplicationContext();
        convertView = inflater.inflate(R.layout.activity_fence, container, false);
        init();
        return convertView;
    }

    private void init() {
        imgSet = (ImageView) convertView.findViewById(R.id.img_activity_options);
        startRealTimeLoc(Constants.LOC_INTERVAL);
        trackApp = (TrackApplication) getActivity().getApplication();
        fenceOperateDialog = new FenceOperateDialog(this);
        fenceHistoryDialog = new FenceHistoryDialog(this);
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) convertView.findViewById(R.id.fence_mapView));
        mapUtil.setCenter(trackApp);
        mapUtil.baiduMap.setOnMapLoadedCallback(this);
        mClusterManager = new ClusterManager<>(getActivity(), mapUtil.baiduMap);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mapUtil.baiduMap.setOnMapStatusChangeListener(mClusterManager);
        mapUtil.baiduMap.setOnMarkerClickListener(mClusterManager);
        initListener();
        trackApp.mClient.startTrace(trackApp.mTrace, traceListener);
    }

    private void initListener() {

        imgSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == fenceSettingDialog) {
                    fenceSettingDialog = new FenceSettingDialog(getActivity(), settingCallback);
                }
                fenceSettingDialog.show();
            }
        });
        trackListener = new OnTrackListener() {

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }

                LatestPoint point = response.getLatestPoint();
                if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                com.baidu.mapapi.model.LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = point.getLocTime();
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                }
            }
        };
        traceListener = new OnTraceListener() {
            @Override
            public void onBindServiceCallback(int i, String s) {
                Log.e("FenceFragment", "onBindServiceCallback s" + s);
            }

            @Override
            public void onStartTraceCallback(int i, String s) {
                Log.e("FenceFragment", "onStartTraceCallback s" + s);
            }

            @Override
            public void onStopTraceCallback(int i, String s) {
                Log.e("FenceFragment", "onStopTraceCallback s" + s);
            }

            @Override
            public void onStartGatherCallback(int i, String s) {
                Log.e("FenceFragment", "onStartGatherCallback s" + s);
            }

            @Override
            public void onStopGatherCallback(int i, String s) {
                Log.e("FenceFragment", "onStopGatherCallback s" + s);
            }

            @Override
            public void onPushCallback(byte b, PushMessage pushMessage) {
                Log.e("FenceFragment", "onPushCallback pushMessage" + pushMessage);
                String message="您的小宝贝"
                        +(pushMessage.getFenceAlarmPushInfo().getMonitoredAction().toString().equals("enter")?"进入":"出去")
                        +pushMessage.getFenceAlarmPushInfo().getFenceName();
                Toast.makeText(trackApp, message, Toast.LENGTH_SHORT).show();
            }
        };


        settingCallback = new FenceSettingDialog.Callback() {
            @Override
            public void onFenceOperateCallback(FenceType fenceType, FenceShape fenceShape,
                                               String fenceName, int vertexesNumber, int operateType) {
                FenceFragment.this.fenceType = fenceType;
                FenceFragment.this.fenceShape = fenceShape;
                FenceFragment.this.fenceName = fenceName;
                FenceFragment.this.vertexesNumber = vertexesNumber;
                switch (operateType) {
                    case R.id.btn_create_fence:
                        mapUtil.baiduMap.setOnMapClickListener(FenceFragment.this);
                        break;
                    case R.id.btn_fence_list:
                        queryFenceList(fenceType);
                        break;
                    default:
                        break;
                }
            }
        };

        createCallback = new FenceCreateDialog.Callback() {

            private int tag;

            @Override
            public void onSureCallback(double radius, int denoise, int offset) {
                FenceFragment.this.radius = radius;
                FenceFragment.this.denoise = denoise;
                FenceFragment.this.offset = offset;

                OverlayOptions overlayOptions = null;
                tag = trackApp.getTag();

                if (FenceShape.circle == fenceShape) {
                    if (FenceType.local == fenceType) {
                        overlayOptions = new CircleOptions().fillColor(0x000000FF).center(circleCenter)
                                .stroke(new Stroke(5, Color.rgb(0x23, 0x19, 0xDC))).radius((int) radius);
                    } else {
                        overlayOptions = new CircleOptions().fillColor(0x000000FF).center(circleCenter)
                                .stroke(new Stroke(5, Color.rgb(0xFF, 0x06, 0x01))).radius((int) radius);
                    }
                    tempLatLngs.put(tag, circleCenter);
                } else if (FenceShape.polygon == fenceShape) {
                    overlayOptions = new PolygonOptions().points(mapVertexes)
                            .stroke(new Stroke(5, 0xFF0601)).fillColor(0xAAFFFF00);
                    tempLatLngs.put(tag, mapVertexes.get(0));
                } else if (FenceShape.polyline == fenceShape) {
                    overlayOptions = new PolylineOptions().points(mapVertexes).width(10)
                            .color(Integer.valueOf(Color.RED));
                    tempLatLngs.put(tag, mapVertexes.get(0));
                }

                tempOverlays.put(tag, mapUtil.baiduMap.addOverlay(overlayOptions));
                mapUtil.baiduMap.setOnMapClickListener(null);
                createFence(tag);
            }

            @Override
            public void onCancelCallback() {
                if (tempOverlays.containsKey(tag)) {
                    tempOverlays.get(tag).remove();
                    tempOverlays.remove(tag);
                }
                for (Overlay overlay : tempMarks) {
                    overlay.remove();
                }
                tempMarks.clear();
                vertexIndex = 0;
                mapUtil.baiduMap.setOnMapClickListener(null);
            }
        };

        fenceListener = new OnFenceListener() {
            @Override
            public void onCreateFenceCallback(CreateFenceResponse response) {
                int tag = response.getTag();
                if (StatusCodes.SUCCESS == response.getStatus()) {
                    String fenceKey = response.getFenceType() + "_" + response.getFenceId();
                    Overlay overlay = tempOverlays.get(tag);
                    String fenceName = response.getFenceName();
                    FenceShape fenceShape = response.getFenceShape();
                    overlays.put(fenceKey, overlay);
                    tempOverlays.remove(tag);
                    Bundle bundle = new Bundle();
                    bundle.putString("fenceKey", fenceKey);
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (tempLatLngs.containsKey(tag)) {
                        mClusterManager.addItem(new MyItem(fenceKey, tempLatLngs.get(tag), fenceName, fenceShape, format.format(date)));
                        mClusterManager.cluster();
                        tempLatLngs.remove(tag);
                    }
                    Log.e("FenceFragment", "onCreateFenceCallback  =" + response.toString());

                    Toast.makeText(getActivity(), "" + getString(R.string.fence_operate_caption), Toast.LENGTH_SHORT).show();
                } else {
                    tempOverlays.get(tag).remove();
                    tempOverlays.remove(tag);
                }
                for (Overlay overlay : tempMarks) {
                    overlay.remove();
                }
                tempMarks.clear();

            }

            @Override
            public void onUpdateFenceCallback(UpdateFenceResponse response) {

            }

            @Override
            public void onDeleteFenceCallback(DeleteFenceResponse response) {
                Log.e("FenceFragment", "onDeleteFenceCallback 666 response.getMessage() =" + response.getMessage());

                List<Long> fenceIds = response.getFenceIds();
                if (null == fenceIds || fenceIds.isEmpty()) {
                    return;
                }

                FenceType fenceType = response.getFenceType();
                Iterator<Map.Entry<String, Overlay>> overlayIt = overlays.entrySet().iterator();
                while (overlayIt.hasNext()) {
                    Map.Entry<String, Overlay> entry = overlayIt.next();
                    long fenceId = Long.parseLong(entry.getKey().split("_")[1]);
                    String fenceKey = fenceType + "_" + fenceId;
                    if (fenceIds.contains(fenceId) && entry.getKey().equals(fenceKey)) {
                        entry.getValue().remove();
                        overlayIt.remove();
                        // 从聚合中删除item
                        mClusterManager.removeItem(new MyItem(fenceKey, new com.baidu.mapapi.model.LatLng(0, 0)));
                    }
                }
                mapUtil.refresh();
            }

            @Override
            public void onFenceListCallback(FenceListResponse response) {
                Log.e("FenceFragment", "onFenceListCallback respone =" + response.toString());
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }
                if (0 == response.getSize()) {
                    StringBuffer message = new StringBuffer("未查询到");
                    if (FenceType.local == response.getFenceType()) {
                        message.append("本地围栏");
                    } else {
                        message.append("服务端围栏");
                    }
                    return;
                }

                FenceType fenceType = response.getFenceType();

                List<FenceInfo> fenceInfos = response.getFenceInfos();
                List<com.baidu.mapapi.model.LatLng> points = new ArrayList<>();
                String fenceKey;
                for (FenceInfo fenceInfo : fenceInfos) {
                    Bundle bundle = new Bundle();
                    String fenceName = fenceInfo.getPolygonFence().getFenceName();
                    FenceShape fenceShape = fenceInfo.getFenceShape();
                    String createTime = fenceInfo.getCreateTime();
                    Log.e("FenceFragment", "fenceName=" + fenceInfo.getPolygonFence().getFenceName());
                    Overlay overlay;
                    switch (fenceInfo.getFenceShape()) {
                        case circle:
                            CircleFence circleFence = fenceInfo.getCircleFence();
                            fenceKey = fenceType + "_" + circleFence.getFenceId();
                            bundle.putString("fenceKey", fenceKey);

                            com.baidu.mapapi.model.LatLng latLng = MapUtil.convertTrace2Map(circleFence.getCenter());
                            double radius = circleFence.getRadius();
                            CircleOptions circleOptions = new CircleOptions().fillColor(0x000000FF).center(latLng)
                                    .radius((int) radius);
                            if (FenceType.local == fenceType) {
                                circleOptions.stroke(new Stroke(5, Color.rgb(0x23, 0x19, 0xDC)));
                                overlay = mapUtil.baiduMap.addOverlay(circleOptions);
                                overlays.put(fenceKey, overlay);
                            } else {
                                circleOptions.stroke(new Stroke(5, Color.rgb(0xFF, 0x06, 0x01)));
                                overlay = mapUtil.baiduMap.addOverlay(circleOptions);
                                overlays.put(fenceKey, overlay);
                            }
                            points.add(latLng);
                            mClusterManager.addItem(new MyItem(fenceKey, latLng, fenceName, fenceShape, createTime));

                            break;

                        case polygon:
                            PolygonFence polygonFence = fenceInfo.getPolygonFence();
                            fenceKey = fenceType + "_" + polygonFence.getFenceId();
                            bundle.putString("fenceKey", fenceKey);
                            List<com.baidu.trace.model.LatLng> polygonVertexes = polygonFence.getVertexes();
                            List<com.baidu.mapapi.model.LatLng> mapVertexes1 = new ArrayList<>();
                            for (com.baidu.trace.model.LatLng ll : polygonVertexes) {
                                mapVertexes1.add(MapUtil.convertTrace2Map(ll));
                            }
                            PolygonOptions polygonOptions = new PolygonOptions().points(mapVertexes1)
                                    .stroke(new Stroke(mapVertexes1.size(), Color.rgb(0xFF, 0x06, 0x01)))
                                    .fillColor(0x30FFFFFF);
                            overlay = mapUtil.baiduMap.addOverlay(polygonOptions);
                            overlays.put(fenceKey, overlay);
                            points.add(mapVertexes1.get(0));
                            mClusterManager.addItem(new MyItem(fenceKey, mapVertexes1.get(0), fenceName, fenceShape, createTime));

                            break;

                        case polyline:
                            PolylineFence polylineFence = fenceInfo.getPolylineFence();
                            fenceKey = fenceType + "_" + polylineFence.getFenceId();
                            bundle.putString("fenceKey", fenceKey);
                            List<com.baidu.trace.model.LatLng> polylineVertexes = polylineFence.getVertexes();
                            List<com.baidu.mapapi.model.LatLng> mapVertexes2 = new ArrayList<>();
                            for (com.baidu.trace.model.LatLng ll : polylineVertexes) {
                                mapVertexes2.add(MapUtil.convertTrace2Map(ll));
                            }
                            PolylineOptions polylineOptions = new PolylineOptions().points(mapVertexes2)
                                    .color(Color.rgb(0xFF, 0x06, 0x01));
                            overlay = mapUtil.baiduMap.addOverlay(polylineOptions);
                            overlays.put(fenceKey, overlay);
                            mClusterManager.addItem(new MyItem(fenceKey, mapVertexes2.get(0), fenceName, fenceShape, createTime));
                            points.add(mapVertexes2.get(0));
                            break;

                        default:
                            break;
                    }

                }

                // 重新聚合
                mClusterManager.cluster();
                mapUtil.animateMapStatus(points);
            }

            @Override
            public void onMonitoredStatusCallback(MonitoredStatusResponse response) {

            }

            @Override
            public void onMonitoredStatusByLocationCallback(MonitoredStatusByLocationResponse response) {

            }

            @Override
            public void onHistoryAlarmCallback(HistoryAlarmResponse response) {

                List<FenceAlarmInfo> fenceAlarmInfos = response.getFenceAlarmInfos();

                for (FenceAlarmInfo fenceAlarmInfo : fenceAlarmInfos) {
                    fenceAlarmInfo.getCurrentPoint().getLocTime();
                }
                fenceHistoryDialog.setTitleText(response.getFenceAlarmInfos());
                fenceHistoryDialog.showAtLocation(convertView.findViewById(R.id.img_activity_options), Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL, 0, 0);
//                Toast.makeText(getActivity(), "" + response.getFenceAlarmInfos(), Toast.LENGTH_SHORT).show();
//                Log.e("FenceFragment", "onHistoryAlarmCallback  response.getFenceAlarmInfos() =" + response.getFenceAlarmInfos());

            }
        };
    }


    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
    }

    class RealTimeLocRunnable implements Runnable {
        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            trackApp.getCurrentLocation(trackListener);
            realTimeHandler.postDelayed(this, interval * 300);
        }
    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    public void onClick(View view) {
        long fenceId;
        String[] fenceKeys;
        FenceType fenceType = FenceType.server;
        switch (view.getId()) {
            // 围栏报警
            case R.id.btn_fenceOperate_alarm:
                HistoryAlarmRequest alarmRequest = null;
                List<Long> alarmFenceIds = new ArrayList<>();
                fenceId = Long.parseLong(fenceKey.split("_")[1]);
                switch (fenceType) {
                    case local:
                        alarmFenceIds.add(fenceId);
                        alarmRequest = HistoryAlarmRequest.buildLocalRequest(trackApp.getTag(),
                                trackApp.serviceId, beginTime, endTime, trackApp.entityName, alarmFenceIds);
                        break;

                    case server:
                        alarmFenceIds.add(fenceId);
                        alarmRequest = HistoryAlarmRequest.buildServerRequest(trackApp.getTag(),
                                trackApp.serviceId, beginTime, endTime,
                                trackApp.entityName, alarmFenceIds, CoordType.bd09ll);
                        break;

                    default:
                        break;
                }
                trackApp.mClient.queryFenceHistoryAlarmInfo(alarmRequest, fenceListener);
                if (fenceOperateDialog != null) {
                    fenceOperateDialog.dismiss();
                }
                break;

            // 删除围栏
            case R.id.btn_fenceOperate_delete:
                List<Long> deleteFenceIds = new ArrayList<>();
                fenceKeys = fenceKey.split("_");
                fenceType = FenceType.valueOf(fenceKeys[0]);
                fenceId = Long.parseLong(fenceKeys[1]);
                deleteFenceIds.add(fenceId);
                DeleteFenceRequest deleteRequest;
                if (FenceType.server == fenceType) {
                    deleteRequest = DeleteFenceRequest.buildServerRequest(trackApp.getTag(),
                            trackApp.serviceId, trackApp.entityName, deleteFenceIds);
                } else {
                    deleteRequest = DeleteFenceRequest.buildLocalRequest(trackApp.getTag(),
                            trackApp.serviceId, trackApp.entityName, deleteFenceIds);
                }
                trackApp.mClient.deleteFence(deleteRequest, fenceListener);
                if (fenceOperateDialog != null) {
                    fenceOperateDialog.dismiss();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        // TODO 实现删除聚合内所有围栏：从cluster中遍历MyItem，获取myItem中的key字段，从key中解析出围栏信息（围栏类型：本地、服务端；围栏编号）。
        return false;
    }

    @Override
    public boolean onClusterItemClick(MyItem item) {
        Log.e("FenceFragment", "itemclick=" + item.toString());
        fenceKey = item.getKey();
        fenceOperateDialog.setTitleText(item.getName(), item.getFenceShape(), item.getPosition(), item.getCreateTime());
        fenceOperateDialog.showAtLocation(convertView.findViewById(R.id.img_activity_options), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        // 处理PopupWindow在Android N系统上的兼容性问题
        if (Build.VERSION.SDK_INT < 24) {
            fenceOperateDialog.update(fenceOperateDialog.getWidth(), fenceOperateDialog.getHeight());
        }
        return false;
    }

    /**
     * 地图加载成功后，查询本地与服务端围栏
     */
    @Override
    public void onMapLoaded() {
        queryFenceList(FenceType.local);
        queryFenceList(FenceType.server);
    }

    /**
     * @param latLng
     */
    @Override
    public void onMapClick(com.baidu.mapapi.model.LatLng latLng) {

        switch (fenceShape) {
            case circle:
                circleCenter = latLng;
                break;

            case polygon:
            case polyline:
                mapVertexes.add(latLng);
                traceVertexes.add(mapUtil.convertMap2Trace(latLng));
                vertexIndex++;
                BitmapUtil.getMark(trackApp, vertexIndex);
                OverlayOptions overlayOptions = new MarkerOptions().position(latLng)
                        .icon(BitmapUtil.getMark(trackApp, vertexIndex)).zIndex(9).draggable(true);
                tempMarks.add(mapUtil.baiduMap.addOverlay(overlayOptions));
                break;

            default:
                break;
        }

        if (null == fenceCreateDialog) {
            fenceCreateDialog = new FenceCreateDialog(getActivity(), createCallback);
        }
        if (FenceShape.circle == fenceShape || vertexIndex == vertexesNumber) {
            fenceCreateDialog.setFenceType(fenceType);
            fenceCreateDialog.setFenceShape(fenceShape);
            fenceCreateDialog.show();
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    private void createFence(int tag) {
        CreateFenceRequest request = null;
        switch (fenceType) {
            case local:
                request = CreateFenceRequest.buildLocalCircleRequest(tag, trackApp.serviceId, fenceName,
                        trackApp.entityName, mapUtil.convertMap2Trace(circleCenter), radius, denoise, CoordType.bd09ll);
                break;

            case server:
                switch (fenceShape) {
                    case circle:
                        request = CreateFenceRequest.buildServerCircleRequest(tag, trackApp.serviceId, fenceName,
                                trackApp.entityName, mapUtil.convertMap2Trace(circleCenter), radius, denoise,
                                CoordType.bd09ll);
                        break;

                    case polygon:
                        request = CreateFenceRequest.buildServerPolygonRequest(tag,
                                trackApp.serviceId, fenceName, trackApp.entityName, traceVertexes, denoise,
                                CoordType.bd09ll);
                        break;

                    case polyline:
                        request = CreateFenceRequest.buildServerPolylineRequest(tag,
                                trackApp.serviceId, fenceName, trackApp.entityName, traceVertexes, offset, denoise,
                                CoordType.bd09ll);
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }

        trackApp.mClient.createFence(request, fenceListener);
    }

    private void queryFenceList(FenceType fenceType) {
        FenceListRequest request = null;
        switch (fenceType) {
            case local:
                request = FenceListRequest.buildLocalRequest(trackApp.getTag(),
                        trackApp.serviceId, trackApp.entityName, null);
                break;

            case server:
                request = FenceListRequest.buildServerRequest(trackApp.getTag(),
                        trackApp.serviceId, trackApp.entityName, null, CoordType.bd09ll);
                break;

            default:
                break;
        }

        trackApp.mClient.queryFenceList(request, fenceListener);
    }

    private void clearOverlay() {
        if (null != overlays) {
            for (Map.Entry<String, Overlay> entry : overlays.entrySet()) {
                entry.getValue().remove();
            }
            overlays.clear();
        }
        if (null != mClusterManager) {
            mClusterManager.clearItems();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearOverlay();
        if (null != fenceCreateDialog) {
            fenceCreateDialog.dismiss();
            fenceCreateDialog = null;
        }
        if (null != fenceOperateDialog) {
            fenceOperateDialog.dismiss();
            fenceOperateDialog = null;
        }
        if (null != fenceSettingDialog) {
            fenceSettingDialog.dismiss();
            fenceSettingDialog = null;
        }
        if (null != mClusterManager) {
            mClusterManager.clearItems();
            mClusterManager = null;
        }
        mapUtil.clear();
        stopRealTimeLoc();
    }


}


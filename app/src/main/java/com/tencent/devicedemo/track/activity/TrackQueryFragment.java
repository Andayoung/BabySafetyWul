package com.tencent.devicedemo.track.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.Point;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.tencent.devicedemo.R;
import com.tencent.devicedemo.track.TrackApplication;
import com.tencent.devicedemo.track.dialog.DateDialog;
import com.tencent.devicedemo.track.utils.CommonUtil;
import com.tencent.devicedemo.track.utils.Constants;
import com.tencent.devicedemo.track.utils.MapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/26 0026.
 */

public class TrackQueryFragment extends Fragment implements DateDialog.Callback {
    private View convertView;
    private TrackApplication trackApp = null;
    private MapUtil mapUtil = null;
    private HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest();
    private OnTrackListener mTrackListener = null;
    private long startTime = CommonUtil.getCurrentTime()-60*60*23;
    private long endTime = CommonUtil.getCurrentTime();
    private List<LatLng> trackPoints = new ArrayList<>();
    private List<Point> speedingPoints = new ArrayList<>();
    private List<Point> harshAccelPoints = new ArrayList<>();
    private List<Point> harshBreakingPoints = new ArrayList<>();
    private List<Point> harshSteeringPoints = new ArrayList<>();
    private List<Point> stayPoints = new ArrayList<>();
    private List<Marker> speedingMarkers = new ArrayList<>();
    private List<Marker> harshAccelMarkers = new ArrayList<>();
    private List<Marker> harshBreakingMarkers = new ArrayList<>();
    private List<Marker> harshSteeringMarkers = new ArrayList<>();
    private List<Marker> stayPointMarkers = new ArrayList<>();
    private SortType sortType = SortType.asc;
    private int pageIndex = 1;
    private Button btnSelect=null;
    private DateDialog dateDialog=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.activity_trackquery, container, false);
        trackApp = (TrackApplication) getActivity().getApplicationContext();
        dateDialog=new DateDialog(getActivity(),this);
        btnSelect=(Button)convertView.findViewById(R.id.btn_select);
        init();
        return convertView;
    }

    /**
     * 初始化
     */
    private void init() {
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) convertView.findViewById(R.id.track_query_mapView));
        mapUtil.setCenter(trackApp);
        initListener();
        queryHistoryTrack();
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });

    }

    /**
     * 查询历史轨迹
     */
    private void queryHistoryTrack() {
        trackApp.initRequest(historyTrackRequest);
        historyTrackRequest.setEntityName(trackApp.entityName);
        historyTrackRequest.setStartTime(startTime);
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, mTrackListener);
    }


    private void clearAnalysisList() {
        if (null != speedingPoints) {
            speedingPoints.clear();
        }
        if (null != harshAccelPoints) {
            harshAccelPoints.clear();
        }
        if (null != harshBreakingPoints) {
            harshBreakingPoints.clear();
        }
        if (null != harshSteeringPoints) {
            harshSteeringPoints.clear();
        }
    }

    private void initListener() {
        mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                int total = response.getTotal();
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    Toast.makeText(getActivity(), response.getMessage(),Toast.LENGTH_SHORT).show();
                } else if (0 == total) {
                    Toast.makeText(getActivity(), getString(R.string.no_track_data),Toast.LENGTH_SHORT).show();
                } else {
                    List<TrackPoint> points = response.getTrackPoints();
                    if (null != points&&trackPoints!=null) {
                        for (TrackPoint trackPoint : points) {
                            if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                    trackPoint.getLocation().getLongitude())) {
                                trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                            }
                        }
                    }
                }

                if (total > Constants.PAGE_SIZE * pageIndex) {
                    historyTrackRequest.setPageIndex(++pageIndex);
                    queryHistoryTrack();
                } else {
                    mapUtil.drawHistoryTrack(trackPoints, sortType);
                }
            }

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                super.onDistanceCallback(response);
            }

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                super.onLatestPointCallback(response);
            }
        };


    }



    /**
     * 清除驾驶行为分析覆盖物
     */
    public void clearAnalysisOverlay() {
        clearOverlays(speedingMarkers);
        clearOverlays(harshAccelMarkers);
        clearOverlays(harshBreakingMarkers);
        clearOverlays(stayPointMarkers);
    }

    private void clearOverlays(List<Marker> markers) {
        if (null == markers) {
            return;
        }
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


        if (null != trackPoints) {
            trackPoints.clear();
        }
        if (null != stayPoints) {
            stayPoints.clear();
        }
        clearAnalysisList();
        trackPoints = null;
        speedingPoints = null;
        harshAccelPoints = null;
        harshSteeringPoints = null;
        stayPoints = null;

        clearAnalysisOverlay();
        speedingMarkers = null;
        harshAccelMarkers = null;
        harshBreakingMarkers = null;
        stayPointMarkers = null;

        mapUtil.clear();
    }


    @Override
    public void onDateCallback(long timeStamp) {
        Log.e("TrackQueryFragment","timeStamp="+timeStamp+","+(timeStamp-24*60*60));
        startTime=timeStamp-24*60*60;
        endTime=timeStamp;
        queryHistoryTrack();
    }
}

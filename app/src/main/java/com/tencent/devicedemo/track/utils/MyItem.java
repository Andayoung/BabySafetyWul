package com.tencent.devicedemo.track.utils;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.trace.api.fence.FenceShape;
import com.baidu.trace.model.LatLng;
import com.tencent.devicedemo.clusterutil.clustering.ClusterItem;

import java.util.List;

/**
 * Created by Administrator on 2017/8/15 0015.
 */
public class MyItem implements ClusterItem {

    private String mKey;
    private String fenceName;
    private FenceShape fenceShape;
    private String createTime;
    private com.baidu.mapapi.model.LatLng mPosition;

    public MyItem(String key, com.baidu.mapapi.model.LatLng latLng, String fenceName, FenceShape fenceShape, String createTime) {
        mKey = key;
        this.fenceShape = fenceShape;
        this.fenceName = fenceName;
        this.createTime = createTime;
        mPosition = latLng;
    }
    public MyItem(String key, com.baidu.mapapi.model.LatLng latLng) {
        mKey = key;
        mPosition = latLng;
    }


    @Override
    public com.baidu.mapapi.model.LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getKey() {
        return mKey;
    }

    public String getName() {
        if (fenceName == null) {
            return "";
        }
        return fenceName;
    }

    public FenceShape getFenceShape() {
        return fenceShape;
    }

    public String getCreateTime() {
        return createTime;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        return BitmapUtil.bmGcoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MyItem myItem = (MyItem) o;

        return mKey.equals(myItem.mKey);

    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    @Override
    public String toString() {
        return "MyItem{mKey='" + mKey + '\'' + ", mPosition=" + mPosition + '}';
    }
}

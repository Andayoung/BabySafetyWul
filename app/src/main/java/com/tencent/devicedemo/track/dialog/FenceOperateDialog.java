package com.tencent.devicedemo.track.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.fence.FenceShape;
import com.tencent.devicedemo.R;
import com.tencent.devicedemo.track.activity.FenceFragment;

import java.util.List;


/**
 * 服务控制对话框
 *
 * @author baidu
 */
public class FenceOperateDialog extends PopupWindow {

    private View mView = null;
    private Button alarmBtn = null;
    private Button deleteBtn = null;
    private Button cancleBtn=null;
    private TextView titleText = null;
    private TextView txtFenceShape=null;
    private TextView txtFenceNote=null;
    private TextView txtFenceTime=null;

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public FenceOperateDialog(final FenceFragment parent) {
        super(parent.getActivity());
        LayoutInflater inflater = (LayoutInflater) parent.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_fence_operate, null);
        alarmBtn = (Button) mView.findViewById(R.id.btn_fenceOperate_alarm);
        deleteBtn = (Button) mView.findViewById(R.id.btn_fenceOperate_delete);
        cancleBtn=(Button)mView.findViewById(R.id.btn_fenceOperate_cancel);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        txtFenceShape=(TextView)mView.findViewById(R.id.txt_fence_shape);
        txtFenceNote=(TextView)mView.findViewById(R.id.txt_fence_note);
        txtFenceTime=(TextView)mView.findViewById(R.id.txt_fence_time);
        alarmBtn.setOnClickListener(parent);
        deleteBtn.setOnClickListener(parent);
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(mView);
        setFocusable(false);
        setOutsideTouchable(false);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.dialog_anim_style);
        setBackgroundDrawable(null);


    }
    public void setTitleText(String title, FenceShape fenceShape, LatLng fenceNotes, String fenceTime){
        Log.e("FenceOperateDialog","title="+title+",fenceShape="+fenceShape.toString()+",fenceNote="+fenceNotes.toString()+",fenceTime="+fenceTime);
        titleText.setText("围栏名称: "+title);
        txtFenceShape.setText("围栏形状："+(fenceShape.toString().equals("polygon")?"多边形":"圆形"));
        String location="围栏位置：";
//        for(LatLng fenceNote:fenceNotes){
            location=location+"经纬度("+(int)fenceNotes.latitude+","+(int)fenceNotes.longitude+") \n";
//        }
        txtFenceNote.setText(location);
        txtFenceTime.setText("建立时间："+fenceTime);
    }

}

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

import com.baidu.trace.api.fence.FenceAlarmInfo;
import com.tencent.devicedemo.R;
import com.tencent.devicedemo.track.activity.FenceFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 服务控制对话框
 *
 * @author baidu
 */
public class FenceHistoryDialog extends PopupWindow {

    private View mView = null;
    private Button cancleBtn=null;
    private TextView txtHistory=null;
    private TextView titleText = null;
    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public FenceHistoryDialog(final FenceFragment parent) {
        super(parent.getActivity());
        LayoutInflater inflater = (LayoutInflater) parent.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_fence_history, null);
        cancleBtn=(Button)mView.findViewById(R.id.btn_fenceOperate_cancel);
        txtHistory=(TextView)mView.findViewById(R.id.txt_history);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        titleText.setText("历史记录");
        setContentView(mView);
        setFocusable(false);
        setOutsideTouchable(false);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.dialog_anim_style);
        setBackgroundDrawable(null);


    }
    public void setTitleText(List<FenceAlarmInfo> content){
        String history="";
        if(content.size()==0){
            history="暂无进出记录";
        }else {
            SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for(FenceAlarmInfo h:content){
                history=history+"您的小宝贝在"+formatter.format(new Date(h.getCurrentPoint().getCreateTime()))
                        +(h.getMonitoredAction().toString().equals("enter")?"进入":"出去")+h.getFenceName()+"围栏\n\n";
//                Log.e("FencehistoryDialog","history="+history);
            }
        }
        txtHistory.setText(history);
    }

}

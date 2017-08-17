package com.tencent.devicedemo.track.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.devicedemo.R;
import com.tencent.devicedemo.track.TrackApplication;
import com.tencent.devicedemo.track.utils.SerialNumberHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/6/14 0014.
 */

public class LogOrRegActivity extends Activity {
    public static final String ZHUCE_URL = "http://115.159.193.122:8080/znsb/QQwlZnsbUser/register.do";
    public static final String DENGLU_URL = "http://115.159.193.122:8080/znsb/QQwlZnsbUser/login.do";
    @BindView(R.id.img_log)
    ImageView imgLog;
    @BindView(R.id.user_name)
    EditText userName;
    @BindView(R.id.user_pwd)
    EditText userPwd;
    @BindView(R.id.denglu)
    Button denglu;
    private SerialNumberHelper serialNumberHelper;
    private TrackApplication trackApp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_log);
        ButterKnife.bind(this);
        trackApp = (TrackApplication)getApplicationContext();
    }


    @OnClick({R.id.denglu})
    public void submit(View btn) {
        if (btn.getId() == R.id.denglu) {
            if (!userName.getText().toString().equals("") && !userPwd.getText().toString().equals("")) {
                login();
            } else {
                Toast.makeText(LogOrRegActivity.this, "请填写完整", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void login() {
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DENGLU_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("MainActivity", "login-s=" + s);
                try {
                    String resultSuccess = new JSONObject(s).getString("success");
                    if (resultSuccess.equals("true")) {
                        JSONObject jsonResult = new JSONObject(s).getJSONObject("data");
                        if (serialNumberHelper == null) {
                            serialNumberHelper = new SerialNumberHelper(getApplicationContext());
                        }
                        serialNumberHelper.save2File(jsonResult.getString("serialNumber")
                        +" "+jsonResult.getString("licence") +" "+userName.getText().toString()
                        +" "+userPwd.getText().toString() +" "+jsonResult.getString("birthdate")
                        +" "+jsonResult.getString("sex"));//"serialNumber":"20160222uu000003"
                        trackApp.entityName=jsonResult.getString("serialNumber");
                        finish();

                    } else {
                        String faultMsg = new JSONObject(s).getString("msg");
                        Toast.makeText(LogOrRegActivity.this, faultMsg, Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    Toast.makeText(LogOrRegActivity.this, "请检查网络是否可用", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("MainActivity", "login-error=" + volleyError);
                Toast.makeText(LogOrRegActivity.this, "请检查网络是否可用", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String md5Secret = null;
                try {
                    md5Secret = getMd5Secret(userPwd.getText().toString());
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("userName", userName.getText().toString());
                    map.put("passWord", md5Secret);
                    return map;
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return null;
                }

            }
        };
        mQueue.add(stringRequest);
    }


    public String getMd5Secret(String pwd) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] cipherData = md5.digest(pwd.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte cipher : cipherData) {
            String toHexStr = Integer.toHexString(cipher & 0xff);
            builder.append(toHexStr.length() == 1 ? "0" + toHexStr : toHexStr);
        }
        Log.e("LogOrRegActivity", "加密=" + builder.toString());
        return builder.toString();
        //c0bb4f54f1d8b14caf6fe1069e5f93ad
    }

}
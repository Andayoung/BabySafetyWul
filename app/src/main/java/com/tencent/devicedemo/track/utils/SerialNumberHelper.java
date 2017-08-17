package com.tencent.devicedemo.track.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/6/22 0022.
 */

public class SerialNumberHelper {
    private Context context;

    public SerialNumberHelper() {
    }

    public SerialNumberHelper(Context context) {
        super();
        this.context = context;
    }

    public void save2File(String text) {
        try {
            File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if(!path.mkdirs()) {
                Log.e("licence", "Directory not created");
            }
            File file=new File(path,"my.uu");
            FileOutputStream output = new FileOutputStream(file);
            output.write(text.getBytes("utf-8"));
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String read4File() {
        StringBuilder sb = new StringBuilder("");
        try {
            File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if(!path.mkdirs()) {
                Log.e("licence", "Directory not created");
            }
            File file=new File(path, "my.uu");
            FileInputStream input = new FileInputStream(file);
            byte[] temp = new byte[1024];
            int len = 0;
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public void deleteFile(){
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(!path.mkdirs()) {
            Log.e("licence", "Directory not created");
        }
        File file=new File(path, "my.uu");
        if(file.isFile()&&file.exists()){
            file.delete();
        }
    }

}
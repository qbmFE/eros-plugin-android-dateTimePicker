package com.qbm.eros_plugin_android_datetimepicker;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.qbm.datetimepicker.widget.CustomDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Context mContext;//
    private TextView txt_hello;
    private static final String SUCCESS = "success";
    private static final String CANCEL = "cancel";
    private static final String ERROR = "error";
    private static final String RESULT = "result";
    private static final String DATA = "data";
    CustomDatePicker customDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        txt_hello = (TextView) findViewById(R.id.txt_hello);
        txt_hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                customDatePicker = new CustomDatePicker(mContext, "2016-02-26 00:00", "2099-12-31 23:59", "123", "#0092ff",
                        "确认", "#0092ff", "取消", "#0092ff", now,
                        new CustomDatePicker.OnPickListener() {

                            @Override
                            public void onPick(boolean set, @Nullable String time) {
                                if (set) {
                                    Map<String, Object> ret = new HashMap<>(2);
                                    ret.put(RESULT, SUCCESS);
                                    ret.put(DATA, time);
                                    txt_hello.setText(time);
//                            callback.invoke(ret);
                                } else {
                                    Map<String, Object> ret = new HashMap<>(2);
                                    ret.put(RESULT, CANCEL);
                                    ret.put(DATA, null);
//                            callback.invoke(ret);
                                }
                            }
                        });
            }
        });
    }
}

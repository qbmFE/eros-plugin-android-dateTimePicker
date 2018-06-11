package com.qbm.datetimepicker.module;

import android.support.annotation.Nullable;

import com.alibaba.weex.plugin.annotation.WeexModule;
import com.qbm.datetimepicker.widget.CustomDatePicker;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.utils.WXResourceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <pre>
 * 项目名：  时间选择
 * 作　者：	niehl
 * 时　间：	2018/6/11
 * 描  述:
 * </pre>
 */
@WeexModule(name = "dateTimePicker", lazyLoad = true)
public class TimePickerModule extends WXModule {

    private static final String SUCCESS = "success";
    private static final String CANCEL = "cancel";
    private static final String ERROR = "error";
    private static final String RESULT = "result";
    private static final String DATA = "data";

    private static final String KEY_VALUE = "value";
    private static final String KEY_INDEX = "index";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MAX = "max";
    private static final String KEY_MIN = "min";
    private static final String KEY_ITEMS = "items";

    private static final String KEY_TITLE_COLOR = "titleColor";
    private static final String KEY_CANCEL_TITLE_COLOR = "cancelTitleColor";
    private static final String KEY_CONFIRM_TITLE = "confirmTitle";
    private static final String KEY_CANCEL_TITLE = "cancelTitle";
    private static final String KEY_CONFIRM_TITLE_COLOR = "confirmTitleColor";

    @JSMethod
    public void open(Map<String, Object> params, JSCallback resultCallback) {
        datePicker(params, resultCallback);
    }

    private void datePicker(Map<String, Object> params, final JSCallback callback) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = sdf.format(new Date());
        String value = getOption(params, KEY_VALUE, now);
        String title = getOption(params, KEY_TITLE, "选择时间");
        String max = getOption(params, KEY_MAX, "2099-12-31 23:59");
        String min = getOption(params, KEY_MIN, "1900-12-31 00:00");
        String titleColor = getOption(params, KEY_TITLE_COLOR, "#0092ff");
        String confirmTitle = getOption(params, KEY_CONFIRM_TITLE, "完成");
        String confirmTitleColor = getOption(params, KEY_CONFIRM_TITLE_COLOR, "#0092ff");
        String cancelTitle = getOption(params, KEY_CANCEL_TITLE, "取消");
        String cancelTitleColor = getOption(params, KEY_CANCEL_TITLE_COLOR, "#0092ff");


        new CustomDatePicker(mWXSDKInstance.getContext(), min, max, title, titleColor,
                confirmTitle, confirmTitleColor, cancelTitle, cancelTitleColor, value, new CustomDatePicker.OnPickListener() {

            @Override
            public void onPick(boolean set, @Nullable String time) {
                if (set) {
                    Map<String, Object> ret = new HashMap<>(2);
                    ret.put(RESULT, SUCCESS);
                    ret.put(DATA, time);
                    callback.invoke(ret);
                } else {
                    Map<String, Object> ret = new HashMap<>(2);
                    ret.put(RESULT, CANCEL);
                    ret.put(DATA, null);
                    callback.invoke(ret);
                }
            }
        });
    }

    private <T> T getOption(Map<String, Object> options, String key, T defValue) {
        Object value = options.get(key);
        if (value == null || value.equals("")) {
            return defValue;
        } else {
            try {
                return (T) value;
            } catch (Exception e) {
                e.printStackTrace();
                return defValue;
            }
        }
    }

    private int getColor(Map<String, Object> options, String key, int defValue) {
        Object value = getOption(options, key, null);
        if (value == null) {
            return defValue;
        }
        return WXResourceUtils.getColor(value.toString(), defValue);
    }

}


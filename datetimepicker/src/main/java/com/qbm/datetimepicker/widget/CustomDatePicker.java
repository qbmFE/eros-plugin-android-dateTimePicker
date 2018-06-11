package com.qbm.datetimepicker.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.qbm.datetimepicker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * <pre>
 * 项目名：  时间选择
 * 作　者：	niehl
 * 时　间：	2018/6/11
 * 描  述:
 * </pre>
 */
public class CustomDatePicker {
    public enum SCROLL_TYPE {
        HOUR(1),
        MINUTE(2);

        SCROLL_TYPE(int value) {
            this.value = value;
        }

        public int value;
    }

    private int scrollUnits = SCROLL_TYPE.HOUR.value + SCROLL_TYPE.MINUTE.value;
    private OnPickListener listener;
    private Context context;
    private boolean canAccess = false;

    private String title;
    private String confirmTitle;
    private String confirmTitleColor;
    private String cancelTitle;
    private String titleColor;
    private String cancelTitleColor;

    private Dialog datePickerDialog;
    private DatePickerView year_pv, month_pv, day_pv, hour_pv, minute_pv;
    private TextView tv_title;//
    private static final int MAX_MINUTE = 59;
    private static final int MAX_HOUR = 23;
    private static final int MIN_MINUTE = 0;
    private static final int MIN_HOUR = 0;
    private static final int MAX_MONTH = 12;

    private ArrayList<String> year, month, day, hour, minute;
    private int startYear, startMonth, startDay, startHour, startMinute, endYear, endMonth, endDay, endHour, endMinute;
    private boolean spanYear, spanMon, spanDay, spanHour, spanMin, identicalYear;
    private Calendar selectedCalender, startCalendar, endCalendar;
    private TextView tv_cancle, tv_select, hour_text, minute_text;

    private String startDate, endDate;

    private int oldSelectedDay, oldSelectedMonth, oldSelectedHour, oldSelectedMinute;

    public interface OnPickListener {
        void onPick(boolean set, String time);
    }

    public CustomDatePicker(Context context, String startDate, String endDate, String title, String titleColor,
                            String confirmTitle, String confirmTitleColor, String cancelTitle, String cancelTitleColor, String value,
                            @NonNull final CustomDatePicker.OnPickListener listener) {
        if (isValidDate(startDate, "yyyy-MM-dd HH:mm") && isValidDate(endDate, "yyyy-MM-dd HH:mm")) {
            canAccess = true;
            this.context = context;
            this.listener = listener;
            this.title = title;
            this.titleColor = titleColor;
            this.confirmTitle = confirmTitle;
            this.confirmTitleColor = confirmTitleColor;
            this.cancelTitle = cancelTitle;
            this.cancelTitleColor = cancelTitleColor;
            this.startDate = startDate;
            this.endDate = endDate;
            selectedCalender = Calendar.getInstance();
            startCalendar = Calendar.getInstance();
            endCalendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            try {
                startCalendar.setTime(sdf.parse(startDate));
                endCalendar.setTime(sdf.parse(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            initDialog();
            initView();

            showSpecificTime(true); // 显示时和分
            setIsLoop(true); // 允许循环滚动
            show(value);
        }
    }

    private void initDialog() {
        if (datePickerDialog == null) {
            datePickerDialog = new Dialog(context, R.style.time_dialog);
//            datePickerDialog.setCancelable(true);
            datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            datePickerDialog.setContentView(R.layout.custom_date_picker);
            Window window = datePickerDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口弹出动画
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = dm.widthPixels;
            window.setAttributes(lp);
        }
    }

    private void initView() {
        year_pv = (DatePickerView) datePickerDialog.findViewById(R.id.year_pv);
        month_pv = (DatePickerView) datePickerDialog.findViewById(R.id.month_pv);
        day_pv = (DatePickerView) datePickerDialog.findViewById(R.id.day_pv);
        hour_pv = (DatePickerView) datePickerDialog.findViewById(R.id.hour_pv);
        minute_pv = (DatePickerView) datePickerDialog.findViewById(R.id.minute_pv);
        tv_cancle = (TextView) datePickerDialog.findViewById(R.id.tv_cancle);
        tv_select = (TextView) datePickerDialog.findViewById(R.id.tv_select);
        hour_text = (TextView) datePickerDialog.findViewById(R.id.hour_text);
        minute_text = (TextView) datePickerDialog.findViewById(R.id.minute_text);

        tv_title = (TextView) datePickerDialog.findViewById(R.id.tv_title);

        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
            if (!TextUtils.isEmpty(titleColor)) {
                tv_title.setTextColor(Color.parseColor(titleColor));
            } else {
                tv_title.setTextColor(context.getResources().getColor(R.color.text1));
            }
        } else {
            tv_title.setText(R.string.title);
            tv_title.setTextColor(context.getResources().getColor(R.color.text1));
        }
        if (!TextUtils.isEmpty(confirmTitle)) {
            tv_select.setText(confirmTitle);
        } else {
            tv_select.setText(R.string.commit);
        }
        tv_select.setTextColor(Color.parseColor(confirmTitleColor));
        if (!TextUtils.isEmpty(cancelTitle)) {
            tv_cancle.setText(cancelTitle);
        } else {
            tv_cancle.setText(R.string.cancle);
        }
        tv_cancle.setTextColor(Color.parseColor(cancelTitleColor));

        tv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPick(false, null);
                datePickerDialog.dismiss();
            }
        });

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                listener.onPick(true, sdf.format(selectedCalender.getTime()));
                datePickerDialog.dismiss();
            }
        });
    }

    private void initParameter() {
        startYear = startCalendar.get(Calendar.YEAR);
        startMonth = startCalendar.get(Calendar.MONTH) + 1;
        startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        startMinute = startCalendar.get(Calendar.MINUTE);
        endYear = endCalendar.get(Calendar.YEAR);
        endMonth = endCalendar.get(Calendar.MONTH) + 1;
        endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
        endMinute = endCalendar.get(Calendar.MINUTE);
        spanYear = startYear != endYear;
        identicalYear = startYear == endYear;
        spanMon = (!spanYear) && (startMonth != endMonth);
        spanDay = (!spanMon) && (startDay != endDay);
        spanHour = (!spanDay) && (startHour != endHour);
        spanMin = (!spanHour) && (startMinute != endMinute);
        Date time = startCalendar.getTime();
        selectedCalender.setTime(time);

    }

    private void initTimer() {
        initArrayList();
        if (spanYear) {
            for (int i = startYear; i <= endYear; i++) {
                year.add(String.valueOf(i));
            }
            for (int i = startMonth; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }

            if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
                hour.add(formatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAX_HOUR; i++) {
                    hour.add(formatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
                minute.add(formatTimeUnit(startMinute));
            } else {
                for (int i = startMinute; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
        } else if (spanMon) {
            year.add(String.valueOf(startYear));
            for (int i = startMonth; i <= endMonth; i++) {
                month.add(formatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }

            if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
                hour.add(formatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAX_HOUR; i++) {
                    hour.add(formatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
                minute.add(formatTimeUnit(startMinute));
            } else {
                for (int i = startMinute; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
        } else if (spanDay) {
            year.add(String.valueOf(startYear));
            month.add(formatTimeUnit(startMonth));
            for (int i = startDay; i <= endDay; i++) {
                day.add(formatTimeUnit(i));
            }

            if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
                hour.add(formatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAX_HOUR; i++) {
                    hour.add(formatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
                minute.add(formatTimeUnit(startMinute));
            } else {
                for (int i = startMinute; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
        } else if (spanHour) {
            year.add(String.valueOf(startYear));
            month.add(formatTimeUnit(startMonth));
            day.add(formatTimeUnit(startDay));

            if ((scrollUnits & SCROLL_TYPE.HOUR.value) != SCROLL_TYPE.HOUR.value) {
                hour.add(formatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= endHour; i++) {
                    hour.add(formatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
                minute.add(formatTimeUnit(startMinute));
            } else {
                for (int i = startMinute; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
        } else if (spanMin) {
            year.add(String.valueOf(startYear));
            month.add(formatTimeUnit(startMonth));
            day.add(formatTimeUnit(startDay));
            hour.add(formatTimeUnit(startHour));

            if ((scrollUnits & SCROLL_TYPE.MINUTE.value) != SCROLL_TYPE.MINUTE.value) {
                minute.add(formatTimeUnit(startMinute));
            } else {
                for (int i = startMinute; i <= endMinute; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
        }
        loadComponent();
    }

    /**
     * 将“0-9”转换为“00-09”
     */
    private String formatTimeUnit(int unit) {
        return unit < 10 ? "0" + String.valueOf(unit) : String.valueOf(unit);
    }

    private void initArrayList() {
        if (year == null) year = new ArrayList<>();
        if (month == null) month = new ArrayList<>();
        if (day == null) day = new ArrayList<>();
        if (hour == null) hour = new ArrayList<>();
        if (minute == null) minute = new ArrayList<>();
        year.clear();
        month.clear();
        day.clear();
        hour.clear();
        minute.clear();
    }

    private void loadComponent() {
        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        hour_pv.setData(hour);
        minute_pv.setData(minute);
        year_pv.setSelected(0);
        month_pv.setSelected(0);
        day_pv.setSelected(0);
        hour_pv.setSelected(0);
        minute_pv.setSelected(0);
        executeScroll();
    }

    private void addListener() {
        year_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
                monthChange();
            }
        });

        month_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, 1);//保证月份不会变化
                selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);

                oldSelectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
                dayChange();
            }
        });

        day_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                oldSelectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
                hourChange();
            }
        });

        hour_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
                oldSelectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
                minuteChange();
            }
        });

        minute_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.MINUTE, Integer.parseInt(text));
                oldSelectedMinute = selectedCalender.get(Calendar.MINUTE);
            }
        });
    }

    private void monthChange() {
        int monthCount = selectedCalender.getActualMaximum(Calendar.MONTH) + 1;
        int temp = oldSelectedMonth - monthCount;
        if (temp > 0) {
            selectedCalender.set(Calendar.MONTH, oldSelectedMonth - temp);
        } else {
            selectedCalender.set(Calendar.MONTH, oldSelectedMonth - 1);
        }
        month.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        if (selectedYear == startYear) {
            for (int i = startMonth; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
        } else if (selectedYear == endYear) {
            for (int i = 1; i <= endMonth; i++) {
                month.add(formatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
        }

        month_pv.setData(month);
        int monthSize = month.size();//控件上显示的日期数（第一个月和最后一个月可能会缺）
        int monthOfYear = selectedCalender.get(Calendar.MONTH) + 1;//当前日期
        if (selectedYear == startYear && monthSize != monthCount) {
            int beginStartMonthSize = monthCount - monthSize;//在起始日期前的月数
            if (monthOfYear < beginStartMonthSize) {
                selectedCalender.set(Calendar.MONTH, beginStartMonthSize);
                month_pv.setSelected(0);
            } else if (monthOfYear == beginStartMonthSize) {
                selectedCalender.set(Calendar.MONTH, monthOfYear);
                month_pv.setSelected(monthOfYear - beginStartMonthSize);
            } else {
                selectedCalender.set(Calendar.MONTH, monthOfYear - 1);
                month_pv.setSelected(monthOfYear - beginStartMonthSize - 1);
            }
        } else if (selectedYear == endYear && (monthOfYear > monthSize)) {
            selectedCalender.set(Calendar.MONTH, monthSize - 1);
            month_pv.setSelected(monthSize - 1);
        } else {
            selectedCalender.set(Calendar.MONTH, monthOfYear - 1);
            month_pv.setSelected(monthOfYear - 1);
        }
        oldSelectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        month_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                dayChange();
            }
        }, 100);
    }

    private void dayChange() {
        int daysCount = selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
        int temp = oldSelectedDay - daysCount;
        if (temp > 0) {//之前选中的日期比当前选中月份的日数还要大
            selectedCalender.set(Calendar.DAY_OF_MONTH, oldSelectedDay - temp);
        } else {
            selectedCalender.set(Calendar.DAY_OF_MONTH, oldSelectedDay);
        }
        day.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        if (selectedYear == startYear && selectedMonth == startMonth) {
            for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        } else if (selectedYear == endYear && selectedMonth == endMonth) {
            for (int i = 1; i <= endDay; i++) {
                day.add(formatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        }
        day_pv.setData(day);
        int daySize = day.size();//控件上显示的日期数（第一个月和最后一个月可能会缺）
        int dayOfMonth = selectedCalender.get(Calendar.DAY_OF_MONTH);//当前日期
        if (selectedYear == startYear && selectedMonth == startMonth && daySize != daysCount) {
            int beginStartDaySize = daysCount - daySize;//在起始日期前的天数
            if (dayOfMonth < beginStartDaySize) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, beginStartDaySize + 1);
                day_pv.setSelected(0);
            } else {
                selectedCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                day_pv.setSelected(dayOfMonth - 1 - beginStartDaySize);
            }

        } else if (selectedYear == endYear && selectedMonth == endMonth && (dayOfMonth > daySize)) {
            selectedCalender.set(Calendar.DAY_OF_MONTH, daySize);
            day_pv.setSelected(daySize - 1);
        } else {
            day_pv.setSelected(dayOfMonth - 1);
        }
        oldSelectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
        day_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                hourChange();
            }
        }, 100);
    }

    private void hourChange() {
        if ((scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value) {
            int hoursCount = selectedCalender.getActualMaximum(Calendar.HOUR_OF_DAY);
            int temp = oldSelectedHour - hoursCount;
            if (temp > 0) {
                selectedCalender.set(Calendar.HOUR_OF_DAY, oldSelectedHour - temp);
            } else {
                selectedCalender.set(Calendar.HOUR_OF_DAY, oldSelectedHour);
            }

            hour.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay) {
                for (int i = startHour; i <= MAX_HOUR; i++) {
                    hour.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay) {
                for (int i = MIN_HOUR; i <= endHour; i++) {
                    hour.add(formatTimeUnit(i));
                }
            } else {
                for (int i = MIN_HOUR; i <= MAX_HOUR; i++) {
                    hour.add(formatTimeUnit(i));
                }
            }
            hour_pv.setData(hour);
            int hourSize = hour.size();//控件上显示的日期数
            int hourOfDay = selectedCalender.get(Calendar.HOUR_OF_DAY);//当前日期
            if (selectedYear == startYear && selectedMonth == startMonth && hourSize != hoursCount) {
                int beginStartHourSize = hoursCount - hourSize;//在起始日期前的天数
                if (hourOfDay < beginStartHourSize) {
                    selectedCalender.set(Calendar.HOUR_OF_DAY, beginStartHourSize);
                    hour_pv.setSelected(0);
                } else if (hourOfDay == beginStartHourSize) {
                    selectedCalender.set(Calendar.HOUR_OF_DAY, hourOfDay + 1);
                    hour_pv.setSelected(hourOfDay - beginStartHourSize);
                } else {
                    selectedCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    hour_pv.setSelected(hourOfDay - beginStartHourSize - 1);
                }

            } else if (selectedYear == endYear && selectedMonth == endMonth && (hourOfDay > hourSize)) {
                selectedCalender.set(Calendar.HOUR_OF_DAY, hourSize);
                hour_pv.setSelected(hourSize);
            } else {
                hour_pv.setSelected(hourOfDay);
            }
            oldSelectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
        }
        hour_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                minuteChange();
            }
        }, 100);

    }

    private void minuteChange() {
        if ((scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value) {
            int minutesCount = selectedCalender.getActualMaximum(Calendar.MINUTE);
            int temp = oldSelectedMinute - minutesCount;
            if (temp > 0) {
                selectedCalender.set(Calendar.MINUTE, oldSelectedMinute - temp);
            } else {
                selectedCalender.set(Calendar.MINUTE, oldSelectedMinute);
            }
            minute.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay && selectedHour == startHour) {
                for (int i = startMinute; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay && selectedHour == endHour) {
                for (int i = MIN_MINUTE; i <= endMinute; i++) {
                    minute.add(formatTimeUnit(i));
                }
            } else {
                for (int i = MIN_MINUTE; i <= MAX_MINUTE; i++) {
                    minute.add(formatTimeUnit(i));
                }
            }
            minute_pv.setData(minute);
            int minuteSize = minute.size();//控件上显示的日期数
            int minteOfHour = selectedCalender.get(Calendar.MINUTE);//当前日期
            if (selectedYear == startYear && selectedMonth == startMonth && selectedHour == startHour && minuteSize != minutesCount) {
                int beginStartMinuteSize = minutesCount - minuteSize;//在起始日期前的天数
                if (minteOfHour < beginStartMinuteSize) {
                    selectedCalender.set(Calendar.MINUTE, beginStartMinuteSize);
                    minute_pv.setSelected(0);
                } else if (minteOfHour == beginStartMinuteSize) {
                    selectedCalender.set(Calendar.MINUTE, minteOfHour + 1);
                    minute_pv.setSelected(minteOfHour - beginStartMinuteSize);
                } else {
                    selectedCalender.set(Calendar.MINUTE, minteOfHour);
                    minute_pv.setSelected(minteOfHour - beginStartMinuteSize - 1);
                }

            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedHour == endHour && (minteOfHour > minuteSize)) {
                selectedCalender.set(Calendar.MINUTE, minuteSize);
                minute_pv.setSelected(minuteSize);
            } else {
                minute_pv.setSelected(minteOfHour);
            }
            oldSelectedMinute = selectedCalender.get(Calendar.MINUTE);
        }
        executeScroll();
    }

    private void executeAnimator(View view) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f);
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ).setDuration(200).start();
    }

    private void executeScroll() {
        year_pv.setCanScroll(year.size() > 1);
        month_pv.setCanScroll(month.size() > 1);
        day_pv.setCanScroll(day.size() > 1);
        hour_pv.setCanScroll(hour.size() > 1 && (scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value);
        minute_pv.setCanScroll(minute.size() > 1 && (scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value);
    }

    private int disScrollUnit(SCROLL_TYPE... scroll_types) {
        if (scroll_types == null || scroll_types.length == 0) {
            scrollUnits = SCROLL_TYPE.HOUR.value + SCROLL_TYPE.MINUTE.value;
        } else {
            for (SCROLL_TYPE scroll_type : scroll_types) {
                scrollUnits ^= scroll_type.value;
            }
        }
        return scrollUnits;
    }

    public void show(String time) {
        if (canAccess) {
            if (isValidDate(time, "yyyy-MM-dd")) {
                if (startCalendar.getTime().getTime() < endCalendar.getTime().getTime()) {
                    canAccess = true;
                    initParameter();
                    initTimer();
                    addListener();
                    setSelectedTime(time);
                    datePickerDialog.show();
                }
            } else {
                canAccess = false;
            }
        }
    }

    /**
     * 设置日期控件是否显示时和分
     */
    public void showSpecificTime(boolean show) {
        if (canAccess) {
            if (show) {
                disScrollUnit();
                hour_pv.setVisibility(View.VISIBLE);
                hour_text.setVisibility(View.VISIBLE);
                minute_pv.setVisibility(View.VISIBLE);
                minute_text.setVisibility(View.VISIBLE);
            } else {
                disScrollUnit(SCROLL_TYPE.HOUR, SCROLL_TYPE.MINUTE);
                hour_pv.setVisibility(View.GONE);
                hour_text.setVisibility(View.GONE);
                minute_pv.setVisibility(View.GONE);
                minute_text.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置日期控件是否可以循环滚动
     */
    public void setIsLoop(boolean isLoop) {
        if (canAccess) {
            this.year_pv.setIsLoop(isLoop);
            this.month_pv.setIsLoop(isLoop);
            this.day_pv.setIsLoop(isLoop);
            this.hour_pv.setIsLoop(isLoop);
            this.minute_pv.setIsLoop(isLoop);
        }
    }

    /**
     * 设置日期控件默认选中的时间
     */
    public void setSelectedTime(String time) {
        if (canAccess) {
            String[] str = time.split(" ");
            String[] dateStr = str[0].split("-");

            year_pv.setSelected(dateStr[0]);
            selectedCalender.set(Calendar.YEAR, Integer.parseInt(dateStr[0]));

            month.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            if (identicalYear) {
                for (int i = startMonth; i <= endMonth; i++) {
                    month.add(formatTimeUnit(i));
                }
            } else if (selectedYear == startYear) {
                for (int i = startMonth; i <= MAX_MONTH; i++) {
                    month.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear) {
                for (int i = 1; i <= endMonth; i++) {
                    month.add(formatTimeUnit(i));
                }
            } else {
                for (int i = 1; i <= MAX_MONTH; i++) {
                    month.add(formatTimeUnit(i));
                }
            }

            month_pv.setData(month);
            month_pv.setSelected(dateStr[1]);
            selectedCalender.set(Calendar.MONTH, Integer.parseInt(dateStr[1]) - 1);
            executeAnimator(month_pv);

            day.clear();
            if (selectedYear == startYear && selectedMonth == startMonth) {
                for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    day.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth) {
                for (int i = 1; i <= endDay; i++) {
                    day.add(formatTimeUnit(i));
                }
            } else {
                for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    day.add(formatTimeUnit(i));
                }
            }
            day_pv.setData(day);
            day_pv.setSelected(dateStr[2]);
            selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr[2]));
            executeAnimator(day_pv);

            if (str.length == 2) {
                String[] timeStr = str[1].split(":");

                if ((scrollUnits & SCROLL_TYPE.HOUR.value) == SCROLL_TYPE.HOUR.value) {
                    hour.clear();
                    int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
                    if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay) {
                        for (int i = startHour; i <= MAX_HOUR; i++) {
                            hour.add(formatTimeUnit(i));
                        }
                    } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay) {
                        for (int i = MIN_HOUR; i <= endHour; i++) {
                            hour.add(formatTimeUnit(i));
                        }
                    } else {
                        for (int i = MIN_HOUR; i <= MAX_HOUR; i++) {
                            hour.add(formatTimeUnit(i));
                        }
                    }
                    hour_pv.setData(hour);
                    hour_pv.setSelected(timeStr[0]);
                    selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr[0]));
                    executeAnimator(hour_pv);
                }

                if ((scrollUnits & SCROLL_TYPE.MINUTE.value) == SCROLL_TYPE.MINUTE.value) {
                    minute.clear();
                    int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
                    int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
                    if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay && selectedHour == startHour) {
                        for (int i = startMinute; i <= MAX_MINUTE; i++) {
                            minute.add(formatTimeUnit(i));
                        }
                    } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay && selectedHour == endHour) {
                        for (int i = MIN_MINUTE; i <= endMinute; i++) {
                            minute.add(formatTimeUnit(i));
                        }
                    } else {
                        for (int i = MIN_MINUTE; i <= MAX_MINUTE; i++) {
                            minute.add(formatTimeUnit(i));
                        }
                    }
                    minute_pv.setData(minute);
                    minute_pv.setSelected(timeStr[1]);
                    selectedCalender.set(Calendar.MINUTE, Integer.parseInt(timeStr[1]));
                    executeAnimator(minute_pv);
                }
            }

            oldSelectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            oldSelectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            oldSelectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
            oldSelectedMinute = selectedCalender.get(Calendar.MINUTE);
            executeScroll();
        }
    }

    /**
     * 验证字符串是否是一个合法的日期格式
     */
    private boolean isValidDate(String date, String template) {
        boolean convertSuccess = true;
        // 指定日期格式
        SimpleDateFormat format = new SimpleDateFormat(template, Locale.CHINA);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2015/02/29会被接受，并转换成2015/03/01
            format.setLenient(false);
            format.parse(date);
        } catch (Exception e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

}


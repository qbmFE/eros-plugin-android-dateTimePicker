package com.qbm.datetimepicker.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.qbm.datetimepicker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <pre>
 * 项目名：  时间选择
 * 作　者：	niehl
 * 时　间：	2018/6/11
 * 描  述:
 * </pre>
 */
public class DatePickerView extends View {

    private Context context;
    /**
     * 控制是否首尾相接循环显示 默认为循环显示
     */
    private boolean loop = true;
    /**
     * text之间间距和minTextSize之比
     */
    public static final float MARGIN_ALPHA = 2.8f;
    /**
     * 自动回滚到中间的速度
     */
    public static final float SPEED = 10;
    private List<String> mDataList;
    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int mCurrentSelected;
    private Paint mPaint, nPaint;
    private float mMaxTextSize = 80;
    private float mMinTextSize = 40;
    private float mMaxTextAlpha = 255;
    private float mMinTextAlpha = 120;
    private int mViewHeight;
    private int mViewWidth;
    private float mLastDownY;
    /**
     * 滑动的距离
     */
    private float mMoveLen = 0;
    private boolean isInit = false;
    private boolean canScroll = true;
    private onSelectListener mSelectListener;
    private Timer timer;
    private MyTimerTask mTask;

    private Canvas canvas;//画布
    private Bitmap bitmap;//位图
    private int mov_x;//声明起点坐标
    private int mov_y;

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Math.abs(mMoveLen) < SPEED) {
                mMoveLen = 0;
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                    performSelect();
                }
            } else {
                // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;
            }
            invalidate();
        }
    };

    public DatePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    private void performSelect() {
        if (mSelectListener != null) {
            mSelectListener.onSelect(mDataList.get(mCurrentSelected));
        }
    }

    public void setData(List<String> datas) {
        mDataList = datas;
        mCurrentSelected = datas.size() / 4;
        invalidate();
    }

    public void setData1(List<String> datas) {
        mDataList = datas;
        mCurrentSelected = datas.size() ;
        invalidate();
    }

    /**
     * 选择选中的item的index
     */
    public void setSelected(int selected) {
        mCurrentSelected = selected;
        if (loop) {
            int distance = mDataList.size() / 2 - mCurrentSelected;
            if (distance < 0) {
                for (int i = 0; i < -distance; i++) {
                    moveHeadToTail();
                    mCurrentSelected--;
                }
            } else if (distance > 0) {
                for (int i = 0; i < distance; i++) {
                    moveTailToHead();
                    mCurrentSelected++;
                }
            }
        }


        invalidate();
    }

    /**
     * 选择选中的内容
     */
    public void setSelected(String mSelectItem) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).equals(mSelectItem)) {
                setSelected(i);
                break;
            }
        }
    }

    private void moveHeadToTail() {
        if (loop) {
            String head = mDataList.get(0);
            mDataList.remove(0);
            mDataList.add(head);
        }
    }

    private void moveTailToHead() {
        if (loop) {
            String tail = mDataList.get(mDataList.size() - 1);
            mDataList.remove(mDataList.size() - 1);
            mDataList.add(0, tail);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
        // 按照View的高度计算字体大小
        mMaxTextSize = mViewHeight / 10f;
        mMinTextSize = mMaxTextSize / 1.6f;
        isInit = true;
        invalidate();
    }

    private void init() {
        timer = new Timer();
        mDataList = new ArrayList<>();
        //第一个paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.SANS_SERIF);
        mPaint.setColor(ContextCompat.getColor(context, R.color.text_color_31));

        //第二个paint
        nPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nPaint.setStyle(Paint.Style.FILL);
        nPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.SANS_SERIF);
        nPaint.setColor(ContextCompat.getColor(context, R.color.text_color_cc));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据index绘制view
        if (isInit) {
            drawData(canvas);
        }
//        Paint p = new Paint();
//        Paint p1 = new Paint();
//        p.setColor(Color.RED);// 设置红色
//        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
//        canvas.drawLine(0, 200, 400, 200, p);// 画线
//        p1.setColor(Color.RED);// 设置红色
//        p1.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
//        canvas.drawLine(0, 280, 400, 280, p1);// 画线
    }

    private void drawData(Canvas canvas) {
        // 先绘制选中的text再往上往下绘制其余的text
        float scale = parabola(mViewHeight / 4.0f, mMoveLen);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(size);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float x = (float) (mViewWidth / 2.0);
        float y = (float) (mViewHeight / 2.0 + mMoveLen);
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));

        canvas.drawText(mDataList.get(mCurrentSelected), x, baseline, mPaint);
        // 绘制上方data
        for (int i = 1; (mCurrentSelected - i) >= 0; i++) {
            drawOtherText(canvas, i, -1);
        }
        // 绘制下方data
        for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
            drawOtherText(canvas, i, 1);
        }
    }

    /**
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    private void drawOtherText(Canvas canvas, int position, int type) {
        float d = MARGIN_ALPHA * mMinTextSize * position + type * mMoveLen;
        float scale = parabola(mViewHeight / 4.0f, d);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        nPaint.setTextSize(size);
        nPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        float y = (float) (mViewHeight / 2.0 + type * d);
        Paint.FontMetricsInt fmi = nPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        canvas.drawText(mDataList.get(mCurrentSelected + type * position),
                (float) (mViewWidth / 2.0), baseline, nPaint);
    }

    /**
     * 抛物线
     *
     * @param zero 零点坐标
     * @param x    偏移量
     */
    private float parabola(float zero, float x) {
        float f = (float) (1 - Math.pow(x / zero, 2));
        return f < 0 ? 0 : f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                doDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                mMoveLen += (event.getY() - mLastDownY);
                if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2) {
                    if (!loop && mCurrentSelected == 0) {
                        mLastDownY = event.getY();
                        invalidate();
                        return true;
                    }
                    if (!loop) {
                        mCurrentSelected--;
                    }
                    // 往下滑超过离开距离
                    moveTailToHead();
                    mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
                } else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2) {
                    if (mCurrentSelected == mDataList.size() - 1) {
                        mLastDownY = event.getY();
                        invalidate();
                        return true;
                    }
                    if (!loop) {
                        mCurrentSelected++;
                    }
                    // 往上滑超过离开距离
                    moveHeadToTail();
                    mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
                }
                mLastDownY = event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                doUp();
                break;
        }
        return true;
    }

    private void doDown(MotionEvent event) {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mLastDownY = event.getY();
    }

    private void doUp() {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(mMoveLen) < 0.0001) {
            mMoveLen = 0;
            return;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 10);
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }
    }

    public interface onSelectListener {
        void onSelect(String text);
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return canScroll && super.dispatchTouchEvent(event);
    }

    /**
     * 控制内容是否首尾相连
     */
    public void setIsLoop(boolean isLoop) {
        loop = isLoop;
    }

}

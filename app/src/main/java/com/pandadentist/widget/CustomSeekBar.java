package com.pandadentist.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.pandadentist.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhangwy on 2017/11/18.
 * Updated by zhangwy on 2017/11/18.
 * Description
 */
@SuppressWarnings("unused")
public class CustomSeekBar extends View {
    private int width;
    private int height;
    private int perWidth = 0;
    private Paint mPaint;
    private Paint mTextPaint;
    private Paint buttonPaint;
    private Bitmap bitmap;
    private Bitmap thumb;
    private Bitmap spot;
    private Bitmap spot_on;
    private int cur_sections = 2;
    private OnSeekBarChangeListener changeListener;
    private int bitMapHeight = 38;//第一个点的起始位置起始，图片的长宽是76，所以取一半的距离
    private int textMove = 60;//字与下方点的距离，因为字体字体是40px，再加上10的间隔
    private int[] colors = new int[]{0xffdf5600, 0x334a90e2};//进度条的橙色,进度条的灰色,字体的灰色
    private int textSize;
    private List<String> sectionTitle;
    private boolean showSectionText = true;
    private boolean canActiveMove = true;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        cur_sections = 0;
        bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        thumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon_seekbar_spot);
        spot = BitmapFactory.decodeResource(getResources(), R.drawable.icon_point_off);
        spot_on = BitmapFactory.decodeResource(getResources(), R.drawable.icon_point_on);
        bitMapHeight = thumb.getHeight() / 2;
        textMove = bitMapHeight + 22;
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);//锯齿不显示
        mPaint.setStrokeWidth(3);
        mTextPaint = new Paint(Paint.DITHER_FLAG);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(0xffb5b5b4);
        buttonPaint = new Paint(Paint.DITHER_FLAG);
        buttonPaint.setAntiAlias(true);
    }

    /**
     * 实例化后调用，设置bar的段数和文字
     */
    public void setSections(List<String> section) {
        if (section != null) {
            sectionTitle = section;
        } else {
            String[] str = new String[]{"低", "中", "高"};
            sectionTitle = new ArrayList<>();
            Collections.addAll(sectionTitle, str);
        }
    }

    public void showSectionText(boolean show) {
        this.showSectionText = show;
    }

    public void canActiveMove(boolean canActiveMove) {
        this.canActiveMove = canActiveMove;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, getResources().getDisplayMetrics());
        setMeasuredDimension(width, height);
        width = width - bitMapHeight / 2;
        perWidth = (width - sectionTitle.size() * spot.getWidth() - thumb.getWidth() / 2) / (sectionTitle.size() - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(0);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
        mPaint.setAlpha(255);
        mPaint.setColor(colors[1]);
        float oldWidth = mPaint.getStrokeWidth();
        mPaint.setStrokeWidth(8);
        canvas.drawLine(bitMapHeight, height * 2 / 3, width /*- bitMapHeight - spot_on.getWidth() / 2*/, height * 2 / 3, mPaint);
        mPaint.setStrokeWidth(oldWidth);
        int section = 0;
        while (section < sectionTitle.size()) {
            if (section < cur_sections) {
                mPaint.setAlpha(255);
//                mPaint.setColor(colors[0]);
//                canvas.drawLine(thumb.getWidth() / 2 + section * perWidth + (section + 1) * spot_on.getWidth(), height * 2 / 3, thumb.getWidth() / 2 + section * perWidth + (section + 1) * spot_on.getWidth() + perWidth, height * 2 / 3, mPaint);
                canvas.drawBitmap(spot_on, thumb.getWidth() / 2 + section * perWidth + section * spot_on.getWidth(), height * 2 / 3 - spot_on.getHeight() / 2, mPaint);
            } else {
                mPaint.setAlpha(255);
                if (section == sectionTitle.size() - 1) {
                    canvas.drawBitmap(spot, width - spot_on.getWidth() - bitMapHeight / 2, height * 2 / 3 - spot.getHeight() / 2, mPaint);
                } else {
                    canvas.drawBitmap(spot, thumb.getWidth() / 2 + section * perWidth + section * spot_on.getWidth(), height * 2 / 3 - spot.getHeight() / 2, mPaint);
                }
            }

            if (this.showSectionText) {
                if (section == sectionTitle.size() - 1) {
                    canvas.drawText(sectionTitle.get(section), width - spot_on.getWidth() - bitMapHeight / 4 - textSize / 2, height * 2 / 3 - textMove, mTextPaint);
                } else {
                    canvas.drawText(sectionTitle.get(section), thumb.getWidth() / 2 + section * perWidth + section * spot_on.getWidth(), height * 2 / 3 - textMove, mTextPaint);
                }
            }
            section++;
        }
        if (cur_sections == sectionTitle.size() - 1) {
            canvas.drawBitmap(thumb, width - spot_on.getWidth() - bitMapHeight / 2 - thumb.getWidth() / 2, height * 2 / 3 - bitMapHeight, buttonPaint);
        } else {
            canvas.drawBitmap(thumb, thumb.getWidth() / 2 + cur_sections * perWidth + cur_sections * spot_on.getWidth() - thumb.getWidth() / 4, height * 2 / 3 - bitMapHeight, buttonPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (!this.canActiveMove) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                thumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon_seekbar_spot);
                int downX = (int) event.getX();
                int downY = (int) event.getY();
                responseTouch(downX, downY);
                if (this.changeListener != null) {
                    this.changeListener.onStartTrackingTouch(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                thumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon_seekbar_spot);
                int moveX = (int) event.getX();
                int moveY = (int) event.getY();
                responseTouch(moveX, moveY);
                if (this.changeListener != null) {
                    this.changeListener.onProgressChanged(this, cur_sections, true);
                }
                break;
            case MotionEvent.ACTION_UP:
                thumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon_seekbar_spot);
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                responseTouch(upX, upY);
                if (this.changeListener != null) {
                    this.changeListener.onStopTrackingTouch(this);
                }
                break;
        }
        return true;
    }

    private void responseTouch(int x, int y) {
        if (x <= width - bitMapHeight / 2) {
            cur_sections = (x + perWidth / 3) / perWidth;
        } else {
            cur_sections = sectionTitle.size() - 1;
        }
        invalidate();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.changeListener = listener;
    }

    //设置进度
    public void setProgress(int progress) {
        cur_sections = progress;
        invalidate();
        if (this.changeListener != null) {
            this.changeListener.onProgressChanged(this, cur_sections, false);
        }
    }

    /**
     * A callback that notifies clients when the progress level has been
     * changed. This includes changes that were initiated by the user through a
     * touch gesture or arrow key/trackball as well as changes that were initiated
     * programmatically.
     */
    public interface OnSeekBarChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param seekBar  The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range 0..max where max
         *                 was set by {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(CustomSeekBar seekBar, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStartTrackingTouch(CustomSeekBar seekBar);

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStopTrackingTouch(CustomSeekBar seekBar);
    }
}

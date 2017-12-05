package com.pandadentist.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;

/**
 * Author: 张维亚
 * 创建时间：2014年6月6日 上午11:06:20
 * 修改时间：2014年6月6日 上午11:06:20
 * Description: 该类是引导图控件类
 **/
public class ZGuideGallery extends Gallery implements OnItemSelectedListener, OnItemClickListener, OnGestureListener {

    public ZGuideGallery(Context context) {
        super(context);
        initListener();
    }

    public ZGuideGallery(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        initListener();
    }

    public ZGuideGallery(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
        initListener();
    }

    private void initListener() {
        this.setOnItemClickListener(this);
        this.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1) {
        if (isScrollingLeft(motionevent, motionevent1)) {
            scrollToLeft();
        } else {
            if (isLast()) {
                onLastAction();
            } else {
                scrollToRight();
            }
        }
        return false;
    }

    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
        return e2.getX() > e1.getX();
    }

    private boolean isLast() {
        return isLast(this.getSelectedItemPosition());
    }

    private boolean isLast(int position) {
        return position + 1 >= this.getCount();
    }

    private void scrollToLeft() {
        onScroll(null, null, -1, 0);
        super.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
    }

    private void scrollToRight() {
        onScroll(null, null, 1, 0);
        onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mRollingListener != null)
            mRollingListener.onPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //不用重写
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isLast(position))
            onLastAction();
    }

    private boolean onLastAction() {
        if (mRollingListener == null)
            return false;
        mRollingListener.onClickLast();
        return true;
    }

    public void setOnGuideRollingListener(OnGuideRollingListener listener) {
        mRollingListener = listener;
    }

    private OnGuideRollingListener mRollingListener;

    public interface OnGuideRollingListener {
        void onPosition(int position);
        void onClickLast();
    }
}
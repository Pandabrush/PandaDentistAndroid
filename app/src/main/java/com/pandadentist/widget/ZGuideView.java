package com.pandadentist.widget;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.pandadentist.R;
import com.pandadentist.util.ZScreen;

/**
 * Author: 张维亚
 * 创建时间：2014年6月6日 上午11:08:40
 * 修改时间：2014年6月6日 上午11:08:40
 * Description: 引导图控件，调用该控件需要初始化资源init(int[] imgIds)，便会自动显示引导图内容；
 * 设置监听setOnGuideListener，当引导图滑动时执行相应操作
 * FSGuideView｛FSGuideGallery，RadioGroup｝
 **/
@SuppressWarnings("unused")
public class ZGuideView extends RelativeLayout implements ZGuideGallery.OnGuideRollingListener {

    //引导图图片展示控件
    private ZGuideGallery mGuideGallery;

    //引导图导航小按钮,也标识着引导图走到第几项
    private RadioGroup mRadioGroup;

    private boolean mShowRadio = true;
    private boolean mShowRadioLast = true;

    //引导图监听事件
    private OnGuideListener mGuideListener;

    public ZGuideView(Context context) {
        super(context);
        initialize();
    }

    public ZGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ZGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_guide, this, true);
        mGuideGallery = (ZGuideGallery) findViewById(R.id.guide_gallery);
        mRadioGroup = (RadioGroup) findViewById(R.id.guide_radio_group);
        this.mShowRadio = mRadioGroup.getVisibility() == VISIBLE;
        this.mShowRadioLast = this.mShowRadio;
    }

    /**
     * @param imgIds   引导图图片资源数组
     * @param listener 回调接口
     */
    public ZGuideView init(ArrayList<Integer> imgIds, OnGuideListener listener) {

        this.mGuideListener = listener;

        this.load(imgIds);

        this.addRadioBtns();

        this.mGuideGallery.setOnGuideRollingListener(this);
        return this;
    }

    private void load(ArrayList<Integer> imgIds) {
        BaseAdapter adapter = new ZBaseAdapter<>(imgIds, mItemLoadingView);
        mGuideGallery.setAdapter(adapter);
        mGuideGallery.setSelection(0);
    }

    private void addRadioBtns() {
        mRadioGroup.removeAllViews();
        LayoutParams params = new LayoutParams(ZScreen.dip2px(getContext(), 15), LayoutParams.WRAP_CONTENT);
        final int count = mGuideGallery.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            addRadioBtn(params, i);
        }
    }

    private void addRadioBtn(LayoutParams params, int position) {
        RadioButton rb = new RadioButton(getContext());
        rb.setButtonDrawable(R.drawable.guide_radiobutton_selector);
        rb.setBackgroundColor(0x00000000);
        rb.setWidth(ZScreen.dip2px(getContext(), 15));
        rb.setId(position);
        rb.setTag(position);
        mRadioGroup.addView(rb, params);
        rb.setSelected(position == 0);
    }

    public void setNoVisibility(boolean visibility) {
        this.mShowRadio = visibility;
        mRadioGroup.setVisibility(this.mShowRadio ? VISIBLE : GONE);
    }

    public void setNoLastVisibility(boolean visibility) {
        this.mShowRadioLast = visibility;
    }

    /**
     * Author: 张维亚
     * 创建时间：2014年6月6日 下午3:07:17
     * 修改时间：2014年6月6日 下午3:07:17
     * Description: 引导图事件监听回调
     **/
    public interface OnGuideListener {
        /**
         * 引导图开始第一页操作
         **/
        void onStart();

        /**
         * 引导图结束操作
         **/
        void onEnd();
    }

    private ZBaseAdapter.OnItemLoadingView<Integer> mItemLoadingView = new ZBaseAdapter.OnItemLoadingView<Integer>() {

        @Override
        public View getView(View convertView, Integer item) {
            return getView(item);
        }

        private View getView(Integer item) {
            ImageView view = new ImageView(getContext());
            view.setImageResource(item);
            view.setScaleType(ImageView.ScaleType.CENTER);
            view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            return view;
        }

    };

    /**
     * @hide
     */
    @Override
    @Deprecated
    public void onPosition(int position) {
        if (position == 0 && mGuideListener != null)
            mGuideListener.onStart();
        mRadioGroup.setVisibility(mShowRadio ? VISIBLE : INVISIBLE);
        if (!mShowRadio)
            return;

        int count = mGuideGallery.getAdapter().getCount();
        if (!mShowRadioLast && position >= (count - 1)) {
            mRadioGroup.setVisibility(INVISIBLE);
            return;
        }

        for (int i = 0; i < count; i++) {
            RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(i);
            radioButton.setSelected(i == position);
        }
    }

    /**
     * @hide
     */
    @Override
    @Deprecated
    public void onClickLast() {
        if (mGuideListener != null)
            mGuideListener.onEnd();
    }
}
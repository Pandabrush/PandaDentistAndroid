package com.pandadentist.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.util.Util;

import java.util.List;

/**
 * Created by zhangwy on 2017/11/18.
 * Updated by zhangwy on 2017/11/18.
 * Description 带有刻度的进度条
 */
@SuppressWarnings("unused")
public class ScaleSeekBar extends LinearLayout {

    private TextView desc;
    private SeekBar seekBar;
    private SeekBar.OnSeekBarChangeListener changeListener;

    public ScaleSeekBar(Context context) {
        super(context);
        this.init();
    }

    public ScaleSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ScaleSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScaleSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        LayoutInflater.from(this.getContext()).inflate(R.layout.widget_progress, this, true);
        this.desc = (TextView) findViewById(R.id.progress_desc);
        this.seekBar = (SeekBar) findViewById(R.id.progress);
        this.seekBar.setOnSeekBarChangeListener(changeListener);
    }

    public ScaleSeekBar setDesc(String desc) {
        if (this.desc != null) {
            this.desc.setText(desc);
        }
        return this;
    }

    public ScaleSeekBar setSections(Integer... sections) {
        if (this.seekBar != null && !Util.isEmpty(sections)) {
//            this.seekBar.setSections(Util.array2List(sections));
            this.seekBar.setMax(sections.length - 1);
        }
        return this;
    }

    public ScaleSeekBar setSections(List<Integer> sections) {
        if (this.seekBar != null && !Util.isEmpty(sections)) {
//            this.seekBar.setSections(sections);
            this.seekBar.setMax(sections.size() - 1);
        }
        return this;
    }

    public ScaleSeekBar setProgress(int progress) {
        if (this.seekBar != null) {
            this.seekBar.setProgress(progress);
        }
        return this;
    }

    public void showSectionText(boolean show) {
//        if (this.seekBar != null) {
//            this.seekBar.showSectionText(show);
//        }
    }

    public void canActiveMove(boolean canActiveMove) {
//        if (this.seekBar != null) {
//            this.seekBar.canActiveMove(canActiveMove);
//        }
    }

    public ScaleSeekBar setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        if (this.seekBar != null) {
            this.seekBar.setOnSeekBarChangeListener(listener);
        }
        this.changeListener = listener;
        return this;
    }
}

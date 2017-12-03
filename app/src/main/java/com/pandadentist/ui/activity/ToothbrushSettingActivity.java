package com.pandadentist.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.SeekBar;

import com.pandadentist.R;
import com.pandadentist.listener.OnModelChangeListener;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.util.Logger;
import com.pandadentist.util.Util;
import com.pandadentist.widget.ScaleSeekBar;
import com.pandadentist.widget.TopBar;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by zhangwy on 2017/11/18.
 * Updated by zhangwy on 2017/11/18.
 * Description
 */

public class ToothbrushSettingActivity extends BaseActivity implements Handler.Callback, OnModelChangeListener, TabLayout.OnTabSelectedListener {

    private static final String EXTRA_BLUE_CONNECT = "blue_connect";

    public static void start(Context context, boolean connect) {
        Intent intent = new Intent(context, ToothbrushSettingActivity.class);
        intent.putExtra(EXTRA_BLUE_CONNECT, connect);
        context.startActivity(intent);
    }

    private final int WHAT_OUT_TIME = 100;
    private TabLayout tabLayout;
    private ScaleSeekBar timeSeekBar;
    private ScaleSeekBar amplitudeSeekBar;
    private ScaleSeekBar intensitySeekBar;
    private BLEProtoProcess bleProtoProcess;
    private OnSettingModelChangeListener onChangeListener;
    private Handler handler = new Handler(this);
    private int timeSeekBarProgress = 0;
    private int amplitudeSeekBarProgress = 0;
    private int intensitySeekBarProgress = 0;
    private int timeSeekBarValue;
    private int amplitudeSeekBarValue;
    private int intensitySeekBarValue;
    private ArrayList<Integer> timeSeekBarArray;
    private ArrayList<Integer> amplitudeSeekBarArray;
    private ArrayList<Integer> intensitySeekBarArray;
    private final int TIME_MIN = 30;
    private final int TIME_MAX = 300;
    private final int TIME_INTERVAL = 30;
    private final int AMPLITUDE_MIN = 3000;
    private final int AMPLITUDE_MAX = 6000;
    private final int AMPLITUDE_INTERVAL = 200;
    private final int INTENSITY_MIN = 200;
    private final int INTENSITY_MAX = 1800;
    private final int INTENSITY_INTERVAL = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UrlDetailActivity.mService != null && !UrlDetailActivity.mService.initialize()) {
            this.showMessage(R.string.toast_msg_un_initialized_service);
            finish();
            return;
        }

        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.topBar.setRightVisibility(false);
            this.topBar.setOnLeftClickListener(new TopBar.OnClickListener() {
                @Override
                public void onClick() {
                    finish();
                }
            });
            this.topBar.setCentreText(R.string.title_toothbrush_setting);
            this.topBar.setCentreTextColor(getResources().getColor(R.color.font_color_toothbrush_default));
        }
        if (!this.isConnect()) {
            this.showNoDevice();
            return;
        }
        this.initTabLayout();
        this.initTimeSeekBar();
        this.initAmplitudeSeekBar();
        this.initIntensitySeekBar();
        this.refreshSeekBarValue(false);
        this.setSeekBarVisibility(false);
        this.requestModel(0xff, 0, 0, 0, 0);
    }

    private boolean isConnect() {
        Intent intent = getIntent();
        return intent != null && intent.hasExtra(EXTRA_BLUE_CONNECT) && intent.getBooleanExtra(EXTRA_BLUE_CONNECT, false);
    }

    private void showNoDevice() {

        findViewById(R.id.toothbrush_setting_icon).setVisibility(View.GONE);
        findViewById(R.id.toothbrush_setting_name).setVisibility(View.GONE);
        findViewById(R.id.toothbrush_setting_models).setVisibility(View.GONE);

        findViewById(R.id.toothbrush_setting_no_device).setVisibility(View.VISIBLE);
        findViewById(R.id.toothbrush_setting_no_device_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_toothbrush_setting;
    }

    private void initTabLayout() {
        this.tabLayout = (TabLayout) findViewById(R.id.toothbrush_setting_tablayout);
        this.tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        this.setSeekBarVisibility(tab.getPosition() == 5);
        switch (tab.getPosition()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                this.requestModel(0, tab.getPosition(), 0, 0, 0);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Logger.d("onTabUnselected" + tab.getText() + "");
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Logger.d("onTabReselected" + tab.getText() + "");
    }

    private void initTimeSeekBar() {
        this.timeSeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_time);
        this.refreshSeekBarDesc(this.timeSeekBar, getString(R.string.toothbrush_time_desc));
        this.timeSeekBarArray = getArray(this.TIME_MIN, this.TIME_MAX, this.TIME_INTERVAL, true);
        this.timeSeekBar.setSections(this.timeSeekBarArray);
        this.timeSeekBar.setProgress(0);
        this.timeSeekBar.canActiveMove(false);
        this.timeSeekBar.setOnSeekBarChangeListener(new OnSettingSeekBarChangeListener(new SeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    timeSeekBarProgress = progress;
                    refreshSeekBarValue(false);
                    requestModelForUser(true);
                }
            }
        }));
    }

    private void initAmplitudeSeekBar() {
        this.amplitudeSeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_amplitude);
        this.refreshSeekBarDesc(this.amplitudeSeekBar, getString(R.string.toothbrush_amplitude_desc));
        this.amplitudeSeekBarArray = getArray(this.AMPLITUDE_MIN, this.AMPLITUDE_MAX, this.AMPLITUDE_INTERVAL, false);
        this.amplitudeSeekBar.setSections(this.amplitudeSeekBarArray);
        this.amplitudeSeekBar.setProgress(0);
        this.amplitudeSeekBar.setOnSeekBarChangeListener(new OnSettingSeekBarChangeListener(new SeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    amplitudeSeekBarProgress = progress;
                    refreshSeekBarValue(false);
                    requestModelForUser(false);
                }
            }
        }));
    }

    private void initIntensitySeekBar() {
        this.intensitySeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_intensity);
        this.refreshSeekBarDesc(this.intensitySeekBar, getString(R.string.toothbrush_intensity_desc));
        this.intensitySeekBarArray = getArray(this.INTENSITY_MIN, this.INTENSITY_MAX, this.INTENSITY_INTERVAL, true);
        this.intensitySeekBar.setSections(this.intensitySeekBarArray);
        this.intensitySeekBar.setProgress(0);
        this.intensitySeekBar.setOnSeekBarChangeListener(new OnSettingSeekBarChangeListener(new SeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    intensitySeekBarProgress = progress;
                    refreshSeekBarValue(false);
                    requestModelForUser(false);
                }
            }
        }));
    }

    private ArrayList<Integer> getArray(int min, int max, int interval, boolean asc) {
        ArrayList<Integer> array = new ArrayList<>();
        if (asc) {
            for (int item = min; item <= max; item += interval) {
                array.add(item);
            }
        } else {
            for (int item = max; item >= min; item -= interval) {
                array.add(item);
            }
        }
        return array;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.clearModelChangeListener();
            this.removeOutTime();
            this.timeSeekBar.setOnSeekBarChangeListener(null);
            this.amplitudeSeekBar.setOnSeekBarChangeListener(null);
            this.intensitySeekBar.setOnSeekBarChangeListener(null);
        } catch (Exception e) {
            Logger.d("onDestroy", e);
        }
    }

    private void requestModelForUser(boolean time) {
        this.requestModel(time ? 3 : 4, 0, this.intensitySeekBarValue, this.amplitudeSeekBarValue, this.timeSeekBarValue);
    }

    /**
     * @param modelType 获取初始化数据modelType = 0xff;设置0-4档位modelType=0;自定义设置modelType=4；自定义设置时间modelType=3
     * @param model     振动档位 不同款对应不一样，0-4
     * @param pwm       占空比  范围 200 - 1800
     * @param tClk      周期  范围(2000(高频)->5000(低频)) 默认3800对应(31000次/min)
     * @param time      刷牙时间  单位s
     */
    private void requestModel(int modelType, int model, int pwm, int tClk, int time) {
        this.showMessage(String.format(Locale.getDefault(), "requestModel(modelType:%1$d, model:%2$d, pwm:%3$d, tClk:%4$d, time:%5$d)", modelType, model, pwm, tClk, time));
        if (this.bleProtoProcess == null)
            bleProtoProcess = UrlDetailActivity.bleProtoProcess == null ? new BLEProtoProcess() : UrlDetailActivity.bleProtoProcess;
        this.clearModelChangeListener();
        this.onChangeListener = new OnSettingModelChangeListener(this);
        bleProtoProcess.setOnModelChangeListener(this.onChangeListener);
        UrlDetailActivity.mService.writeRXCharacteristic(bleProtoProcess.getModel((byte) modelType, (short) model, (short) pwm, (short) tClk, (short) time));
        this.sendOutTime();
    }

    private void sendOutTime() {
        this.removeOutTime();
        if (this.handler != null) {
            this.handler.sendEmptyMessageDelayed(WHAT_OUT_TIME, 3000);
        }
    }

    private void removeOutTime() {
        if (this.handler != null && this.handler.hasMessages(WHAT_OUT_TIME)) {
            this.handler.removeMessages(WHAT_OUT_TIME);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_OUT_TIME:
                this.clearModelChangeListener();
                break;
        }
        return false;
    }

    private void clearModelChangeListener() {
        if (this.onChangeListener != null) {
            this.onChangeListener.destroy();
        }
    }

    /**
     * modelType = 0 model 为1-4档，modelType != 0为自定义
     * modelResult成功与否
     * 根据pwm/tClk/time滑动滑块
     *
     * @param modelType   振动类型， 0-按档位设定 1-改变周期 2-改变幅度 3-时间，4-同时改变周期和幅度和时间
     * @param model       振动档位 不同款对应不一样，0-4
     * @param pwm         占空比  范围 200 - 1800
     * @param tClk        周期  范围(2000(高频)->5000(低频)) 默认3800对应(31000次/min)
     * @param time        刷牙时间  单位s
     * @param modelResult 结果
     */
    @Override
    public void onModelChange(int modelType, int model, int pwm, int tClk, int time, int modelResult) {
        this.showMessage("onModelChange");
        this.removeOutTime();
        int position = modelType == 0 ? model : 5;
        TabLayout.Tab tab = this.tabLayout.getTabAt(position);
        if (tab != null) {
            tab.select();
        }
        this.setSeekBarVisibility(modelType != 0);
        this.timeSeekBarProgress = this.findPosition(this.timeSeekBarArray, this.correctionValue(time, this.TIME_INTERVAL, this.TIME_MIN, this.TIME_MAX));
        this.amplitudeSeekBarProgress = this.findPosition(this.amplitudeSeekBarArray, this.correctionValue(tClk, this.AMPLITUDE_INTERVAL, this.AMPLITUDE_MIN, this.AMPLITUDE_MAX));
        this.intensitySeekBarProgress = this.findPosition(this.intensitySeekBarArray, this.correctionValue(pwm, this.INTENSITY_INTERVAL, this.INTENSITY_MIN, this.INTENSITY_MAX));
        this.refreshSeekBarValue(true);
    }

    private int correctionValue(int src, int interval, int min, int max) {
        if (src <= min)
            return min;
        if (src >= max)
            return max;
        return Math.round((src - min) / (float) interval) * interval + min;
    }

    private void refreshSeekBarValue(boolean showMsg) {
        int ctimeSeekBarValue = timeSeekBarValue;
        int camplitudeSeekBarValue = amplitudeSeekBarValue;
        int cintensitySeekBarValue = intensitySeekBarValue;

        this.timeSeekBarValue = findValue(timeSeekBarArray, timeSeekBarProgress, this.TIME_MIN);
        this.amplitudeSeekBarValue = findValue(amplitudeSeekBarArray, amplitudeSeekBarProgress, this.AMPLITUDE_MIN);
        this.intensitySeekBarValue = findValue(intensitySeekBarArray, intensitySeekBarProgress, this.INTENSITY_MIN);

        if (showMsg) {
            String base = "%1$s :   %2$d    :   %3$d    :   %4$d";
            this.showMessage(String.format(Locale.getDefault(), base, "timeSeekBarArray", this.timeSeekBarProgress, this.timeSeekBarValue, ctimeSeekBarValue));
            this.showMessage(String.format(Locale.getDefault(), base, "amplitudeSeekBarArray", this.amplitudeSeekBarProgress, this.amplitudeSeekBarValue, camplitudeSeekBarValue));
            this.showMessage(String.format(Locale.getDefault(), base, "intensitySeekBarArray", this.intensitySeekBarProgress, this.intensitySeekBarValue, cintensitySeekBarValue));
        }

        {
            int minutes = this.timeSeekBarValue / 60;
            int seconds = this.timeSeekBarValue % 60;
            this.refreshSeekBarDesc(this.timeSeekBar, seconds == 0 ? getString(R.string.toothbrush_time_desc, minutes) : getString(R.string.toothbrush_time_desc_mmss, minutes, seconds));
            this.refreshSeekBarProgress(this.timeSeekBar, this.timeSeekBarProgress);
        }
        {
            this.refreshSeekBarDesc(this.amplitudeSeekBar, getString(R.string.toothbrush_amplitude_desc, 2 * 60 * 1000 * 1000 / this.amplitudeSeekBarValue));
            this.refreshSeekBarProgress(this.amplitudeSeekBar, this.amplitudeSeekBarProgress);
        }
        {
            this.refreshSeekBarDesc(this.intensitySeekBar, getString(R.string.toothbrush_intensity_desc, this.intensitySeekBarProgress + 1));
            this.refreshSeekBarProgress(this.intensitySeekBar, this.intensitySeekBarProgress);
        }
    }

    private void refreshSeekBarDesc(ScaleSeekBar seekBar, String desc) {
        if (seekBar != null) {
            seekBar.setDesc(desc);
        }
    }

    private void refreshSeekBarProgress(ScaleSeekBar seekBar, int progress) {
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }

    private void setSeekBarVisibility(boolean show) {
        if (this.timeSeekBar != null)
            this.timeSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (this.amplitudeSeekBar != null)
            this.amplitudeSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (this.intensitySeekBar != null)
            this.intensitySeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 从数组中找到对应的值所在的位置
     *
     * @param array 数组
     * @param value 值
     * @return 位置
     */
    private int findPosition(ArrayList<Integer> array, int value) {
        if (!Util.isEmpty(array)) {
            for (int position = 0; position < array.size(); position++) {
                if (array.get(position) == value)
                    return position;
            }
        }
        return 0;
    }

    private int findValue(ArrayList<Integer> array, int position, int min) {
        if (!Util.isEmpty(array)) {
            if (position < array.size()) {
                return array.get(position);
            } else {
                return array.get(0);
            }
        }
        return min;
    }

    private static class OnSettingModelChangeListener implements OnModelChangeListener {

        private WeakReference<ToothbrushSettingActivity> reference;

        private OnSettingModelChangeListener(ToothbrushSettingActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        public void onModelChange(int modelType, int model, int pwm, int tClk, int time, int modelResult) {

            ToothbrushSettingActivity activity = get();
            if (activity != null) {
                activity.onModelChange(modelType, model, pwm, tClk, time, modelResult);
            }
        }

        private ToothbrushSettingActivity get() {
            if (this.reference == null)
                return null;
            return this.reference.get();
        }

        private void destroy() {
            if (this.reference != null) {
                this.reference.clear();
            }
        }
    }

    private static class OnSettingSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private int progress = 0;
        private boolean fromUser = false;
        private SoftReference<SeekBarChangeListener> reference;

        private OnSettingSeekBarChangeListener(SeekBarChangeListener listener) {
            this.reference = new SoftReference<>(listener);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.progress = progress;
            this.fromUser = fromUser;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            SeekBarChangeListener seekBarChangeListener;
            if (fromUser && (seekBarChangeListener = this.get()) != null) {
                seekBarChangeListener.onProgressChanged(seekBar, this.progress, this.fromUser);
            }
        }

        private SeekBarChangeListener get() {
            return this.reference == null ? null : this.reference.get();
        }
    }

    private interface SeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
    }
}

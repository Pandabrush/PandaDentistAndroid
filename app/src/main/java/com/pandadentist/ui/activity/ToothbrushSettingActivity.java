package com.pandadentist.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.listener.OnModelChangeListener;
import com.pandadentist.util.BLEProtoProcess;
import com.pandadentist.util.Logger;
import com.pandadentist.widget.ScaleSeekBar;
import com.pandadentist.widget.TopBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by zhangwy on 2017/11/18.
 * Updated by zhangwy on 2017/11/18.
 * Description
 */

public class ToothbrushSettingActivity extends BaseActivity implements Handler.Callback, OnModelChangeListener, TabLayout.OnTabSelectedListener {

    public static void start(Context context) {
        context.startActivity(new Intent(context, ToothbrushSettingActivity.class));
    }

    private final int WHAT_OUT_TIME = 100;
    private TabLayout tabLayout;
    private ScaleSeekBar cycleSeekBar;
    private ScaleSeekBar amplitudeSeekBar;
    private ScaleSeekBar intensitySeekBar;
    private BLEProtoProcess bleProtoProcess;
    private OnSettingModelChangeListener onChangeListener;
    private Handler handler = new Handler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        this.initTabLayout();
        this.initCycleSeekBar();
        this.initAmplitudeSeekBar();
        this.initIntensitySeekBar();
        this.requestModel(0xff, 0, 0, 0, 0);
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
        switch (tab.getPosition()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                this.requestModel(0, tab.getPosition(), 0, 0, 0);
                break;
            case 5:
                //TODO:
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

    private void initCycleSeekBar() {
        this.cycleSeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_cycle);
        this.refreshSeekBarDesc(this.cycleSeekBar, getString(R.string.toothbrush_cycle_desc));
        ArrayList<String> array = getArray(30, 300, 30);
        this.cycleSeekBar.setSections(array);
        this.cycleSeekBar.setProgress(0);
        this.cycleSeekBar.canActiveMove(false);
    }

    private void initAmplitudeSeekBar() {
        this.amplitudeSeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_amplitude);
        this.refreshSeekBarDesc(this.amplitudeSeekBar, getString(R.string.toothbrush_amplitude_desc));
        ArrayList<String> array = getArray(3000, 6000, 200);
        this.amplitudeSeekBar.setSections(array);
        this.amplitudeSeekBar.setProgress(0);
    }

    private void initIntensitySeekBar() {
        this.intensitySeekBar = (ScaleSeekBar) findViewById(R.id.toothbrush_setting_intensity);
        this.refreshSeekBarDesc(this.intensitySeekBar, getString(R.string.toothbrush_intensity_desc));
        ArrayList<String> array = getArray(200, 1800, 100);
        this.intensitySeekBar.setSections(array);
        this.intensitySeekBar.setProgress(0);
    }

    private void refreshSeekBarDesc(ScaleSeekBar seekBar, String desc) {
        if (seekBar != null) {
            seekBar.setDesc(desc);
        }
    }

    private ArrayList<String> getArray(int min, int max, int interval) {
        ArrayList<String> array = new ArrayList<>();
        for (int item = min; item <= max; item += interval) {
            array.add(String.valueOf(item));
        }
        return array;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.clearModelChangeListener();
        this.removeOutTime();
    }

    /**
     * @param modelType 获取初始化数据modelType = 0xff;设置0-4档位modelType=0;自定义设置modelType=4；自定义设置时间modelType=3
     * @param model     振动档位 不同款对应不一样，0-4
     * @param pwm       占空比  范围 200 - 1800
     * @param tClk      周期  范围(2000(高频)->5000(低频)) 默认3800对应(31000次/min)
     * @param time      刷牙时间  单位s
     */
    private void requestModel(int modelType, int model, int pwm, int tClk, int time) {
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
        this.removeOutTime();
        //TODO: 设置值
        Toast.makeText(this, "modelType=" + modelType + "\nmodel=" + model + "\npwm=" + pwm + "\ntClk=" + tClk + "\ntime=" + time + "\nmodelResult=" + modelResult, Toast.LENGTH_SHORT).show();
//        int position = modelType == 0 ? model : 5;
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

        public void destroy() {
            if (this.reference != null) {
                this.reference.clear();
            }
        }
    }
}

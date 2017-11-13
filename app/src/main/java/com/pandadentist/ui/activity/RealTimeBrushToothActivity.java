package com.pandadentist.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.pandadentist.R;
import com.pandadentist.util.Toasts;
import com.pandadentist.widget.X5ObserWebView;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import butterknife.Bind;

/**
 * Created by fudaye on 2017/9/9.
 * Updated by zhangwy on 2017/11/12
 */

public class RealTimeBrushToothActivity extends SwipeRefreshBaseActivity {

    private static final String url = "http://www.easylinkage.cn/webapp/shuaya/app_index.html";
    @Bind(R.id.wv)
    X5ObserWebView mWebView;

    private boolean isLoadFinish = false;
    private boolean isReceiveNotify = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.hasTopBar()) {
            this.topBar.setLeftVisibility(true);
            this.setOnLeftClickListener();
            this.topBar.setCentreVisibility(false);
            this.topBar.setRightVisibility(false);
        }
        loadUrl(url);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("easylinkage://tooth?getReplayData");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toasts.showShort("启动是否是发达");
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_read_time_brush_tooth;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        setRefresh(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                setRefresh(false);
                isLoadFinish = true;
                uploadData();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ("easylinkage://tooth?getReplayData".equals(url)) {
                    isReceiveNotify = false;
                    uploadData();
                }
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());

        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);

        mWebView.loadUrl(url);
    }

    private void uploadData() {
        if (isLoadFinish && isReceiveNotify) {
            loadUrl("javascript:alertMessage(\"" + "s" + "\"  ,\"" + "s" + "\" )");
        }
    }
}

package com.pandadentist.network;


import com.pandadentist.config.AppConfig;
import com.pandadentist.bleconnection.utils.Logger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ford on 2016/5/25 0025.
 * Updated by zhangwy
 */
public class APIFactory {
    public <T> T create(Class c){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Logger.d("message-->"+message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(logging);
        builder.connectTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
         return (T)retrofit.create(c);
    }
}

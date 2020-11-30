package com.artiwares.common.http;

import android.annotation.SuppressLint;

import com.artiwares.common.utils.LogUtils;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class OkHttpClientManager {

    public static final String TAG = "OkHttp";
    public static HashMap<String, String> headers;
    public static Interceptor interceptor;

    /**
     * @param interceptor 预留其他需求需要扩展拦截器
     */
    public static void setInterceptor(Interceptor interceptor) {
        OkHttpClientManager.interceptor = interceptor;
    }

    public static void setHeaders(HashMap<String, String> headers) {
        OkHttpClientManager.headers = headers;
    }

    /**
     * 添加拦截器之前需要添加请求头
     *
     * @return OkHttpClient
     */
    public static OkHttpClient getClient() throws NullPointerException {
        if (headers == null) {
            throw new NullPointerException("headers can not be null");
        }

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> {// 拦截 信息 并打印出来
            LogUtils.w(TAG, message);
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        @SuppressLint("TrustAllX509TrustManager")
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new X509TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new HeaderInterceptor(headers))
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(DO_NOT_VERIFY);
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

}
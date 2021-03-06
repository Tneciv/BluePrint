package com.tneciv.blueprint.common;

import android.content.Context;

import com.tneciv.blueprint.BuildConfig;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.tneciv.blueprint.common.utils.SystemUtil.getDiskCachePath;
import static com.tneciv.blueprint.common.utils.SystemUtil.isNetworkReachable;

/**
 * Created by Tneciv
 * on 2016-09-11 15:24 .
 */

public class OkhttpFacade {

    private static volatile OkHttpClient.Builder defaultInstance;
    private static final int TIMEOUT_SECONDS = 30;

    private OkhttpFacade() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    public static OkHttpClient.Builder getInstance(Context context) {

        if (defaultInstance == null) {
            synchronized (OkHttpClient.Builder.class) {
                if (defaultInstance == null) {
                    defaultInstance = new OkHttpClient.Builder();
                }
            }

            /**
             * Okhttp Log 信息拦截器
             */
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
                defaultInstance.addInterceptor(loggingInterceptor);
            }

            /**
             * Okhttp 缓存拦截器
             */
            Interceptor cacheInterceptor = chain -> {

                Request request = chain.request();
                if (!isNetworkReachable(context)) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }

                Response response = chain.proceed(request);
                Response responseLast;
                if (isNetworkReachable(context)) {
                    int maxAge = 0;
                    // when net available , set cache out of date time to 0 .
                    responseLast = response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    int maxStale = 60 * 60 * 24 * 28;
                    //  when net not available , set cache out of date time to 4 weeks .
                    responseLast = response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }

                return responseLast;

            };

            // Cache Path
            File httpCacheDirectory = new File(getDiskCachePath(context), "responses");
            // Cache Size
            Cache cache = new Cache(httpCacheDirectory, 20 * 1024 * 1024);
            defaultInstance.cache(cache);

            defaultInstance.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            defaultInstance.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            defaultInstance.writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            defaultInstance.addInterceptor(cacheInterceptor);
            defaultInstance.addNetworkInterceptor(cacheInterceptor);
            defaultInstance.retryOnConnectionFailure(false);

        }

        return defaultInstance;

    }

}

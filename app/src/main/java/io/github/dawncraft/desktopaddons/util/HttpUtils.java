package io.github.dawncraft.desktopaddons.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.pluto.plugins.network.okhttp.PlutoOkhttpHelperKt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.model.UserModel;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public final class HttpUtils
{
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final HttpUrl DAWNCRAFT_API = new HttpUrl.Builder().scheme("https").host("api.dawncraft.cc").build();
    private static final List<String> FORCE_CACHE_URLS = Arrays.asList("view.inews.qq.com");
    private static OkHttpClient client;
    private static CustomTabsIntent.Builder customTabsBuilder;

    private HttpUtils() {}

    public static void init(Context context)
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .authenticator(new DawnAuthenticator())
                .addInterceptor(new AuthInterceptor())
                .addNetworkInterceptor(new CacheInterceptor());
        PlutoOkhttpHelperKt.addPlutoOkhttpInterceptor(builder);
        client = builder.build();
        customTabsBuilder = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM);
                /* 有亿点点丑
                .setDefaultColorSchemeParams(
                        new CustomTabColorSchemeParams.Builder()
                                .setToolbarColor(context.getColor(R.color.colorPrimary))
                                .setSecondaryToolbarColor(context.getColor(R.color.colorPrimaryDark))
                                .build()
                );
                 */
    }

    /**
     * @deprecated Only use for test
     */
    @Deprecated
    public static void testInit()
    {
        client = new OkHttpClient.Builder()
                .authenticator(new DawnAuthenticator())
                .addInterceptor(new AuthInterceptor())
                .addNetworkInterceptor(new CacheInterceptor())
                .build();
    }

    public static OkHttpClient getClient()
    {
        return client;
    }

    public static void openUrl(Context context, String url, boolean newTask)
    {
        CustomTabsIntent customTabsIntent = customTabsBuilder.build();
        // NOTE 踩坑了! 在Activity之外startActivity时必须用FLAG_ACTIVITY_NEW_TASK参数
        // 详见android.app.ContextImpl#startActivity(android.content.Intent, android.os.Bundle)
        if (newTask)
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static class DawnAuthenticator implements Authenticator
    {
        @Override
        public Request authenticate(Route route, @NonNull Response response) throws IOException
        {
            if (response.request().url().host().equals(DAWNCRAFT_API.host()))
            {
                // NOTE 后端在未登录时请求logout会返回401
                if (response.request().url().toString().contains("/logout"))
                    return null;
                DAApplication.removeToken();
            }
            return null;
        }
    }

    public static class AuthInterceptor implements Interceptor
    {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException
        {
            Request request = chain.request();
            if (request.url().host().equals(DAWNCRAFT_API.host()))
            {
                // NOTE 避免刷新token时无限递归
                if (!request.url().toString().contains("/user/refreshToken") && DAApplication.needRefresh())
                {
                    UserModel userModel = new UserModel();
                    userModel.refreshToken();
                }
                String token = DAApplication.getToken();
                if (token != null)
                {
                    request = request.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                }
            }
            return chain.proceed(request);
        }
    }

    public static class CacheInterceptor implements Interceptor
    {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Response response = chain.proceed(chain.request());
            if (FORCE_CACHE_URLS.contains(chain.request().url().host()))
            {
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "max-age=60")
                        .build();
            }
            return response;
        }
    }
}

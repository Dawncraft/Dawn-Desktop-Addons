package io.github.dawncraft.desktopaddons.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

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

    private HttpUtils() {}

    public static void init(Context context)
    {
        client = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .authenticator(new DawnAuthenticator())
                .addInterceptor(new AuthInterceptor())
                .addNetworkInterceptor(new CacheInterceptor())
                .build();
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

    public static class DawnAuthenticator implements Authenticator
    {
        @Override
        public Request authenticate(Route route, @NonNull Response response) throws IOException
        {
            if (response.request().url().host().equals(DAWNCRAFT_API.host()))
            {
                // NOTE 我的后端在未登录时请求logout会返回401
                if (response.request().url().toString().contains("/logout"))
                    return null;
                UserModel userModel = new UserModel();
                String username = DAApplication.getPreferences().getString("username", null);
                String password = DAApplication.getPreferences().getString("password", null);
                if (userModel.syncLogin(username, password) == UserModel.LoginResult.SUCCESS)
                    return response.request();
                userModel.logout(null);
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
                String token = DAApplication.getPreferences().getString("token", null);
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

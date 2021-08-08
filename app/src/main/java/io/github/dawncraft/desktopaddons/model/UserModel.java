package io.github.dawncraft.desktopaddons.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserModel
{
    private Request getLoginRequest(String username, String password)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("username", username);
            json.put("password", password);
        }
        catch (JSONException ignored) {}
        return new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder().addPathSegment("login").build())
                .post(RequestBody.create(json.toString(), HttpUtils.JSON))
                .build();
    }

    private LoginResult handleLoginResponse(Response response) throws IOException
    {
        String content = Objects.requireNonNull(response.body()).string();
        try
        {
            JSONObject json = new JSONObject(content);
            int code = json.getInt("code");
            switch (code)
            {
                case 1001: return LoginResult.INVALID_USERNAME;
                case 1002: return LoginResult.INVALID_PASSWORD;
                case 1010: return LoginResult.WRONG_USER_OR_PASSWORD;
                case 1011: return LoginResult.ALREADY_LOGIN;
            }
            JSONObject data = json.getJSONObject("data");
            String token = data.getString("token");
            DAApplication.getPreferences().edit().putString("token", token).apply();
            return LoginResult.SUCCESS;
        }
        catch (JSONException ignored) {}
        return LoginResult.ERROR;
    }

    public LoginResult syncLogin(String username, String password)
    {
        if (TextUtils.isEmpty(username))
            return LoginResult.INVALID_USERNAME;
        if (TextUtils.isEmpty(password))
            return LoginResult.INVALID_PASSWORD;
        try
        {
            Request request = getLoginRequest(username, password);
            Response response = HttpUtils.getClient().newCall(request).execute();
            return handleLoginResponse(response);
        }
        catch (IOException ignored) {}
        return LoginResult.ERROR;
    }

    public void asyncLogin(String username, String password, OnLoginListener listener)
    {
        if (TextUtils.isEmpty(username))
        {
            listener.onLoginResult(LoginResult.INVALID_USERNAME);
            return;
        }
        if (TextUtils.isEmpty(password))
        {
            listener.onLoginResult(LoginResult.INVALID_PASSWORD);
            return;
        }
        Request request = getLoginRequest(username, password);
        HttpUtils.getClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                listener.onLoginResult(LoginResult.ERROR);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                listener.onLoginResult(handleLoginResponse(response));
            }
        });
    }

    public void logout(Consumer<Boolean> consumer)
    {
        Request request = new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder().addPathSegment("logout").build())
                .get()
                .build();
        HttpUtils.getClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                DAApplication.getPreferences().edit().remove("password").remove("token").apply();
                if (consumer != null) consumer.accept(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                DAApplication.getPreferences().edit().remove("password").remove("token").apply();
                if (consumer != null) consumer.accept(true);
            }
        });
    }

    public static boolean isLoggedIn()
    {
        return DAApplication.getPreferences().contains("token");
    }

    public enum LoginResult
    {
        SUCCESS(R.string.login_success),
        ALREADY_LOGIN(R.string.already_login),
        INVALID_USERNAME(R.string.invalid_account),
        INVALID_PASSWORD(R.string.invalid_password),
        WRONG_USER_OR_PASSWORD(R.string.wrong_user_or_password),
        ERROR(R.string.login_error);

        @StringRes
        private final int message;

        LoginResult(@StringRes int resId)
        {
            message = resId;
        }

        @StringRes
        public int getMessage()
        {
            return message;
        }
    }

    public interface OnLoginListener
    {
        void onLoginResult(LoginResult result);
    }
}

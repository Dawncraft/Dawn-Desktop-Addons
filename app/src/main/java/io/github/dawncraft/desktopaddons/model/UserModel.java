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
    public void login(String username, String password, OnLoginListener listener)
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
        JSONObject json = new JSONObject();
        try
        {
            json.put("username", username);
            json.put("password", password);
        }
        catch (JSONException ignored) {}
        Request request =  new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder().addPathSegment("login").build())
                .post(RequestBody.create(json.toString(), HttpUtils.JSON))
                .build();
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
                String content = Objects.requireNonNull(response.body()).string();
                try
                {
                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("code");
                    switch (code)
                    {
                        case 1001: listener.onLoginResult(LoginResult.INVALID_USERNAME); return;
                        case 1002: listener.onLoginResult(LoginResult.INVALID_PASSWORD); return;
                        case 1010: listener.onLoginResult(LoginResult.WRONG_USER_OR_PASSWORD); return;
                        case 1011: listener.onLoginResult(LoginResult.ALREADY_LOGIN); return;
                    }
                    JSONObject data = json.getJSONObject("data");
                    String token = data.getString("token");
                    DAApplication.setToken(token);
                    listener.onLoginResult(LoginResult.SUCCESS);
                    return;
                }
                catch (JSONException ignored) {}
                listener.onLoginResult(LoginResult.ERROR);
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
                DAApplication.removeToken();
                if (consumer != null) consumer.accept(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                DAApplication.removeToken();
                if (consumer != null) consumer.accept(true);
            }
        });
    }

    public boolean refreshToken()
    {
        try
        {
            Request request = new Request.Builder()
                    .url(HttpUtils.DAWNCRAFT_API.newBuilder()
                            .addPathSegment("user")
                            .addPathSegment("refreshToken")
                            .build())
                    .get()
                    .build();
            Response response = HttpUtils.getClient().newCall(request).execute();
            String content = Objects.requireNonNull(response.body()).string();
            JSONObject json = new JSONObject(content);
            if (json.getInt("code") == 200)
            {
                JSONObject data = json.getJSONObject("data");
                String token = data.getString("token");
                DAApplication.setToken(token);
                return true;
            }
        }
        catch (IOException | JSONException ignored) {}
        return false;
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

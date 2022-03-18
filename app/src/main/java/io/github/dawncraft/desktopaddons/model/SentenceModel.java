package io.github.dawncraft.desktopaddons.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SentenceModel
{
    public Sentence getHitokoto() throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url("https://v1.hitokoto.cn/")
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        return parseHitokoto(new JSONObject(content));
    }

    public Sentence getSentence() throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder().addPathSegment("sentence").build())
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        JSONObject json = new JSONObject(content);
        return parseSentence(json.getJSONObject("data"));
    }

    public Sentence getSentence(int id) throws IOException, JSONException
    {
        Request request = new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder()
                        .addPathSegments("sentence/get")
                        .addQueryParameter("id", String.valueOf(id - 1))
                        .addQueryParameter("count", "1")
                        .build())
                .get()
                .build();
        Response response = HttpUtils.getClient().newCall(request).execute();
        String content = Objects.requireNonNull(response.body()).string();
        JSONObject json = new JSONObject(content);
        JSONArray data = json.getJSONArray("data");
        return parseSentence(data.getJSONObject(0));
    }

    public void getSentences(int id, int count, OnSentencesListener listener)
    {
        Request request = new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder()
                        .addPathSegments("sentence/get")
                        .addQueryParameter("id", String.valueOf(id))
                        .addQueryParameter("count", String.valueOf(count))
                        .build())
                .get()
                .build();
        HttpUtils.getClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                listener.onSentences(null);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                String content = Objects.requireNonNull(response.body()).string();
                try
                {
                    JSONObject json = new JSONObject(content);
                    JSONArray data = json.getJSONArray("data");
                    List<Sentence> sentences = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++)
                    {
                        Sentence sentence = parseSentence(data.getJSONObject(i));
                        sentences.add(sentence);
                    }
                    listener.onSentences(sentences);
                }
                catch (JSONException e)
                {
                    listener.onSentences(null);
                }
            }
        });
    }

    public void addSentence(String sentence, String author, String from, OnEditSentenceListener listener)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("sentence", sentence);
            json.put("author", author);
            if (!TextUtils.isEmpty(from))
                json.put("from", from);
        }
        catch (JSONException ignored) {}
        Request request = new Request.Builder()
                .url(HttpUtils.DAWNCRAFT_API.newBuilder().addPathSegments("sentence/add").build())
                .post(RequestBody.create(json.toString(), HttpUtils.JSON))
                .build();
        HttpUtils.getClient().newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                listener.onResult(false, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                String content = Objects.requireNonNull(response.body()).string();
                try
                {
                    JSONObject json = new JSONObject(content);
                    int code = json.getInt("code");
                    String message = json.getString("msg");
                    listener.onResult(code == 0, message);
                }
                catch (JSONException e)
                {
                    listener.onResult(false, e.getLocalizedMessage());
                }
            }
        });
    }

    private static Sentence parseHitokoto(JSONObject json) throws JSONException
    {
        if (json == null) return null;
        Sentence sentence = new Sentence();
        sentence.setSource(Sentence.Source.Hitokoto);
        sentence.setId(json.getInt("id"));
        sentence.setUUID(json.getString("uuid"));
        sentence.setSentence(json.getString("hitokoto"));
        if (!json.isNull("from_who"))
            sentence.setAuthor(json.getString("from_who"));
        sentence.setFrom(json.getString("from"));
        return sentence;
    }

    private static Sentence parseSentence(JSONObject json) throws JSONException
    {
        if (json == null) return null;
        Sentence sentence = new Sentence();
        sentence.setSource(Sentence.Source.Dawncraft);
        sentence.setId(json.getInt("id"));
        sentence.setUUID(String.valueOf(sentence.getId()));
        sentence.setSentence(json.getString("sentence"));
        if (!json.isNull("author"))
            sentence.setAuthor(json.getString("author"));
        if (!json.isNull("from"))
            sentence.setFrom(json.getString("from"));
        return sentence;
    }

    public interface OnSentencesListener
    {
        void onSentences(List<Sentence> sentences);
    }

    public interface OnEditSentenceListener
    {
        void onResult(boolean success, String message);
    }
}

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
        JSONObject json = new JSONObject(content);
        Sentence sentence = new Sentence();
        sentence.setSource(Sentence.Source.Hitokoto);
        sentence.setId(json.getInt("id"));
        sentence.setSentence(json.getString("hitokoto"));
        sentence.setAuthor(json.getString("from_who"));
        sentence.setFrom(json.getString("from"));
        return sentence;
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
        JSONObject data = json.getJSONObject("data");
        Sentence sentence = new Sentence();
        sentence.setSource(Sentence.Source.Dawncraft);
        sentence.setId(data.getInt("id"));
        sentence.setSentence(data.getString("sentence"));
        sentence.setAuthor(data.getString("author"));
        sentence.setFrom(data.getString("from"));
        return sentence;
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
                        JSONObject jsonSentence = data.getJSONObject(i);
                        Sentence sentence = new Sentence();
                        sentence.setSource(Sentence.Source.Dawncraft);
                        sentence.setId(jsonSentence.getInt("id"));
                        sentence.setSentence(jsonSentence.getString("sentence"));
                        sentence.setAuthor(jsonSentence.getString("author"));
                        sentence.setFrom(jsonSentence.getString("from"));
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

    public interface OnSentencesListener
    {
        void onSentences(List<Sentence> sentences);
    }

    public interface OnEditSentenceListener
    {
        void onResult(boolean success, String message);
    }
}

package io.github.dawncraft.desktopaddons.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.util.Utils;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends AppCompatActivity
{
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        if (url == null)
        {
            Utils.toast(this, R.string.invalid_url);
            finish();
            return;
        }
        setContentView(R.layout.activity_web_view);
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.open_in_browser)
                .setIcon(R.drawable.ic_open_in_browser)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == Menu.FIRST)
        {
            Utils.openUrl(this, url);
            return true;
        }
        return false;
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url)
        {
            setTitle(view.getTitle());
        }
    }
}

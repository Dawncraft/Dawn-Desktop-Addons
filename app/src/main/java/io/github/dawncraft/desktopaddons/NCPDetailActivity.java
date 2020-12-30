package io.github.dawncraft.desktopaddons;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("SetJavaScriptEnabled")
public class NCPDetailActivity extends AppCompatActivity
{
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");
        if (url == null)
        {
            Utils.toast(this, "无效的网址");
            finish();
            return;
        }
        setContentView(R.layout.activity_ncp_detail);
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "在浏览器中打开")
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

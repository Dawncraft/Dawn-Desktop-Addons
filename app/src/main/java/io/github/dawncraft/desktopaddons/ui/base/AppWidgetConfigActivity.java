package io.github.dawncraft.desktopaddons.ui.base;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.util.Utils;

public class AppWidgetConfigActivity extends AppCompatActivity
{
    protected Bundle extras;
    protected int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 读取桌面小部件id
        extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
        {
            Utils.toast(this, R.string.invalid_app_widget);
            finish();
            return;
        }
        appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        setResult(RESULT_CANCELED, getResultValue());
    }

    protected void applyConfig()
    {
        setResult(RESULT_OK, getResultValue());
        finish();
    }

    private Intent getResultValue()
    {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return intent;
    }
}

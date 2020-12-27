package io.github.dawncraft.desktopaddons;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WidgetConfigActivity extends AppCompatActivity
{
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 桌面小部件
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null)
        {
            Utils.toast(this, "无效的桌面小部件配置页");
            finish();
            return;
        }
        appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        setResult(RESULT_CANCELED, getResultValue());
        // 初始化视图
        setContentView(R.layout.activity_ncp_app_widget_config);
        NCPInfoModel.EnumResult result = NCPInfoModel.loadData();
        if (result != NCPInfoModel.EnumResult.SUCCESS && result != NCPInfoModel.EnumResult.CACHED)
        {
            Utils.toast(this, "无法读取疫情数据" + result.toString());
            finish();
            return;
        }
        List<String> areaList = NCPInfoModel.getRegions();
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, areaList.toArray(new String[0]));
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(listAdapter);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        // TODO 点击
    }

    public void onButtonClicked(View v)
    {
        if (v.getId() == R.id.buttonConfirm)
        {
            setResult(RESULT_OK, getResultValue());
            finish();
        }
        else if (v.getId() == R.id.buttonCancel)
        {
            finish();
        }
    }

    private Intent getResultValue()
    {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return intent;
    }
}

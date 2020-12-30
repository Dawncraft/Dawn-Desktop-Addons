package io.github.dawncraft.desktopaddons;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WidgetConfigActivity extends AppCompatActivity
{
    private int appWidgetId;
    private List<String> areaList;

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
        final NCPInfoModel.EnumResult[] result = new NCPInfoModel.EnumResult[] { NCPInfoModel.EnumResult.UNKNOWN };
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    result[0] = NCPInfoModel.loadData();
                }
            });
            thread.start();
            thread.join();
        }
        catch (InterruptedException ignored) {}
        NCPAppWidget.printResult(this, result[0]);
        if (result[0] != NCPInfoModel.EnumResult.SUCCESS && result[0] != NCPInfoModel.EnumResult.CACHED)
        {
            finish();
            return;
        }
        areaList = NCPInfoModel.getRegions();
        if (areaList == null || areaList.isEmpty())
        {
            Utils.toast(this, "获取地区列表失败");
            finish();
            return;
        }
        Button button = findViewById(R.id.buttonConfirm);
        button.setEnabled(false);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, areaList.toArray(new String[0]));
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(listAdapter);
        autoCompleteTextView.setThreshold(1);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                parent.setSelection(position);
                button.setEnabled(true);
            }
        });
    }

    public void onButtonClicked(View v)
    {
        if (v.getId() == R.id.buttonConfirm)
        {
            ListView listView = findViewById(R.id.listView);
            String area = areaList.get(listView.getCheckedItemPosition());
            // TODO 用ContentProvider保存APPWidget的id和area
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

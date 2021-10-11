package io.github.dawncraft.desktopaddons.ui;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.appwidget.NCPAppWidget;
import io.github.dawncraft.desktopaddons.entity.NCPAppWidgetID;
import io.github.dawncraft.desktopaddons.entity.QQNewsNCPInfo;
import io.github.dawncraft.desktopaddons.model.NCPInfoModel;
import io.github.dawncraft.desktopaddons.ui.adapter.AreaAdapter;
import io.github.dawncraft.desktopaddons.ui.adapter.AutoCompleteAdapter;
import io.github.dawncraft.desktopaddons.util.Utils;

public class NCPAppWidgetConfigActivity extends AppCompatActivity
{
    private Button buttonConfirm;
    private AutoCompleteAdapter<String> autoCompleteAdapter;
    private ListView listView;
    private AreaAdapter areaAdapter;

    private Handler handler;
    private NCPInfoModel ncpInfoModel;

    private int appWidgetId;
    private List<QQNewsNCPInfo> rootArea;
    private Deque<QQNewsNCPInfo> areaPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        ncpInfoModel = new NCPInfoModel();
        // 读取桌面小部件id
        Bundle extras = getIntent().getExtras();
        if (extras == null)
        {
            Utils.toast(this, R.string.invalid_app_widget);
            finish();
            return;
        }
        appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        setResult(RESULT_CANCELED, getResultValue());
        // 初始化视图
        setContentView(R.layout.activity_ncp_info_config);
        autoCompleteAdapter = new AutoCompleteAdapter<>(this, android.R.layout.simple_list_item_1);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = autoCompleteAdapter.getItem(position);
                String[] areas = item.split(",");
                areaPath.clear();
                QQNewsNCPInfo selected = null;
                if (areas.length > 1)
                {
                    areaPath.add(findInList(rootArea, areas[0]));
                    for (int i = 1; i < areas.length; i++)
                    {
                        QQNewsNCPInfo ncpInfo = findInList(areaPath.getLast().getChildren(), areas[i]);
                        if (i == areas.length - 1)
                        {
                            selected = ncpInfo;
                            break;
                        }
                        areaPath.add(ncpInfo);
                    }
                }
                else
                {
                    selected = findInList(rootArea, areas[0]);
                }
                updateViews();
                int newPos = areaPath.getLast().getChildren().indexOf(selected);
                listView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listView.smoothScrollToPosition(newPos);
                    }
                });
            }
        });
        autoCompleteTextView.setAdapter(autoCompleteAdapter);
        areaAdapter = new AreaAdapter();
        areaAdapter.setOnAreaClickListener(new AreaAdapter.OnAreaClickListener()
        {
            @Override
            public void onSubareaButtonClick(QQNewsNCPInfo ncpInfo)
            {
                areaPath.add(ncpInfo);
                updateViews();
            }
        });
        listView = findViewById(R.id.listView);
        // NOTE ListView#setOnItemSelectedListener(AdapterView.OnItemSelectedListener) 无效
        // 详见 https://www.jianshu.com/p/d11f86051c20
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                buttonConfirm.setEnabled(true);
            }
        });
        listView.setAdapter(areaAdapter);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<String> areas = new ArrayList<>();
                for (QQNewsNCPInfo ncpInfo : areaPath)
                {
                    areas.add(ncpInfo.getArea());
                }
                areas.add(((QQNewsNCPInfo) areaAdapter.getItem(listView.getCheckedItemPosition())).getArea());
                String region = TextUtils.join(",", areas);
                NCPAppWidgetID ncpAppWidgetID = new NCPAppWidgetID();
                ncpAppWidgetID.id = appWidgetId;
                ncpAppWidgetID.region = region;
                DAApplication.getDatabase().ncpAppWidgetDAO().insert(ncpAppWidgetID);
                NCPAppWidget.notifyUpdate(NCPAppWidgetConfigActivity.this, new int[] { appWidgetId });
                setResult(RESULT_OK, getResultValue());
                finish();
            }
        });
        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        // 更新区域数据
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ncpInfoModel.getAllInfo(new NCPInfoModel.OnAllDataListener()
                {
                    @Override
                    public void onResponse(NCPInfoModel.Result result, List<QQNewsNCPInfo> infoTree)
                    {
                        areaPath = new LinkedList<>();
                        rootArea = infoTree;
                        if (result != NCPInfoModel.Result.SUCCESS)
                        {
                            QQNewsNCPInfo ncpInfo = new QQNewsNCPInfo();
                            ncpInfo.setArea("中国");
                            rootArea = Collections.singletonList(ncpInfo);
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Utils.toast(NCPAppWidgetConfigActivity.this, R.string.get_area_list_failed);
                                }
                            });
                        }
                        initAutoComplete();
                        handler.post(NCPAppWidgetConfigActivity.this::updateViews);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed()
    {
        if (!areaPath.isEmpty())
        {
            areaPath.removeLast();
            updateViews();
            return;
        }
        super.onBackPressed();
    }

    // TODO 腾讯新闻最多只有3层所以暂时写死, 改成递归
    // dbq我又写垃圾代码了, 但是我想不动用递归应该怎么写了
    private void initAutoComplete()
    {
        List<String> pathList = new ArrayList<>();
        for (QQNewsNCPInfo ncpInfo1 : rootArea)
        {
            String area1 = ncpInfo1.getArea();
            if (ncpInfo1.getChildren() != null)
            {
                for (QQNewsNCPInfo ncpInfo2 : ncpInfo1.getChildren())
                {
                    String area2 = ncpInfo2.getArea();
                    if (ncpInfo2.getChildren() != null)
                    {
                        for (QQNewsNCPInfo ncpInfo3 : ncpInfo2.getChildren())
                        {
                            String area3 = ncpInfo3.getArea();
                            pathList.add(area1 + "," + area2 + "," + area3);
                        }
                    }
                    pathList.add(area1 + "," + area2);
                }
            }
            pathList.add(area1);
        }
        autoCompleteAdapter.setList(pathList);
    }

    private void updateViews()
    {
        buttonConfirm.setEnabled(false);
        if (areaPath.isEmpty())
        {
            areaAdapter.setAreaList(rootArea, false);
        }
        else
        {
            areaAdapter.setAreaList(areaPath.getLast().getChildren(), false);
        }
        listView.setAdapter(areaAdapter);
    }

    private Intent getResultValue()
    {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return intent;
    }

    private static QQNewsNCPInfo findInList(List<QQNewsNCPInfo> list, String area)
    {
        for (QQNewsNCPInfo ncpInfo : list)
        {
            if (ncpInfo.getArea().equals(area))
                return ncpInfo;
        }
        return null;
    }
}

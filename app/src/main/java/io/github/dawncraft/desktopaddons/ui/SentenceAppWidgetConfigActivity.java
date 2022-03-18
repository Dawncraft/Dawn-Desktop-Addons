package io.github.dawncraft.desktopaddons.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.appwidget.SentenceAppWidget;
import io.github.dawncraft.desktopaddons.dao.SentenceAppWidgetDAO;
import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.entity.SentenceAppWidgetID;
import io.github.dawncraft.desktopaddons.model.SentenceModel;
import io.github.dawncraft.desktopaddons.ui.base.AppWidgetConfigActivity;
import io.github.dawncraft.desktopaddons.util.Utils;

public class SentenceAppWidgetConfigActivity extends AppWidgetConfigActivity
{
    private Handler handler;
    private SentenceAppWidgetDAO sentenceAppWidgetDAO;
    private SentenceModel sentenceModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        sentenceAppWidgetDAO = DAApplication.getDatabase().sentenceAppWidgetDAO();
        sentenceModel = new SentenceModel();
        SentenceAppWidgetID oldSentenceAppWidgetID = sentenceAppWidgetDAO.findById(appWidgetId);
        if (oldSentenceAppWidgetID == null)
        {
            oldSentenceAppWidgetID = new SentenceAppWidgetID();
            oldSentenceAppWidgetID.id = appWidgetId;
            oldSentenceAppWidgetID.source = Sentence.Source.Hitokoto;
        }
        // 初始化视图
        setContentView(R.layout.activity_sentence_config);
        setFinishOnTouchOutside(false);
        Spinner spinnerSource = findViewById(R.id.spinnerSource);
        spinnerSource.setSelection(oldSentenceAppWidgetID.source.ordinal());
        EditText editTextNumber = findViewById(R.id.editTextSentence);
        editTextNumber.setText(oldSentenceAppWidgetID.sid);
        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        SentenceAppWidgetID sentenceAppWidgetID = oldSentenceAppWidgetID;
        buttonConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sentenceAppWidgetID.source = Sentence.Source.values()[spinnerSource.getSelectedItemPosition()];
                sentenceAppWidgetID.sid = editTextNumber.getText().toString();
                if (TextUtils.isEmpty(sentenceAppWidgetID.sid))
                {
                    sentenceAppWidgetDAO.insert(sentenceAppWidgetID);
                    applyConfig();
                }
                else
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (sentenceAppWidgetID.source != Sentence.Source.Dawncraft)
                            {
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Utils.toast(SentenceAppWidgetConfigActivity.this, R.string.sentence_get_not_implemented);
                                    }
                                });
                                return;
                            }
                            Sentence sentence = getSentence(sentenceAppWidgetID.sid);
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (sentence == null)
                                    {
                                        Utils.toast(SentenceAppWidgetConfigActivity.this, R.string.sentence_not_found);
                                        return;
                                    }
                                    sentenceAppWidgetDAO.insert(sentenceAppWidgetID);
                                    applyConfig();
                                }
                            });
                        }
                    }).start();
                }
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
    }

    @Override
    protected void applyConfig()
    {
        SentenceAppWidget.notifyUpdate(this, new int[] { appWidgetId });
        super.applyConfig();
    }

    private Sentence getSentence(String sentenceId)
    {
        try
        {
            return sentenceModel.getSentence(Integer.parseInt(sentenceId));
        }
        catch (Exception ignored) {}
        return null;
    }
}

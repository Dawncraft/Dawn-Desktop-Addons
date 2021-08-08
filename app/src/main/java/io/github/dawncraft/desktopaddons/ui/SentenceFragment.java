package io.github.dawncraft.desktopaddons.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yl.recyclerview.listener.OnScrollListener;
import com.yl.recyclerview.wrapper.LoadMoreWrapper;

import java.util.List;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.model.SentenceModel;
import io.github.dawncraft.desktopaddons.model.UserModel;
import io.github.dawncraft.desktopaddons.ui.adapter.SentenceAdapter;
import io.github.dawncraft.desktopaddons.util.Utils;

// TODO 临时解决方案, 以后如果有机会再重构
public class SentenceFragment extends Fragment
{
    private final int COUNT_PER_PAGE = 20;

    private RecyclerView recyclerViewSentences;
    private SentenceAdapter sentenceAdapter;
    private LoadMoreWrapper loadMoreWrapper;

    private SentenceModel sentenceModel;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sentenceModel = new SentenceModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_sentence, container, false);
        sentenceAdapter = new SentenceAdapter();
        loadMoreWrapper = new LoadMoreWrapper(sentenceAdapter);
        ProgressBar progressBar = new ProgressBar(getActivity());
        loadMoreWrapper.setLoadingView(progressBar);
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.no_more_sentences);
        loadMoreWrapper.setLoadingEndView(textView);
        recyclerViewSentences = root.findViewById(R.id.recyclerViewSentences);
        recyclerViewSentences.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewSentences.addOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onLoadMore()
            {
                loadMoreWrapper.setLoadStateNotify(loadMoreWrapper.LOADING);
                loadMoreSentences();
            }
        });
        recyclerViewSentences.setAdapter(loadMoreWrapper);
        FloatingActionButton buttonAdd = root.findViewById(R.id.floatingActionButton);
        buttonAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!UserModel.isLoggedIn())
                {
                    Utils.toast(getContext(), R.string.sentence_need_login);
                    return;
                }
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_add_sentence, null, false);
                EditText editTextSentence = view.findViewById(R.id.editTextSentence);
                EditText editTextAuthor = view.findViewById(R.id.editTextAuthor);
                EditText editTextFrom = view.findViewById(R.id.editTextFrom);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(R.string.add_sentence)
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String sentence = editTextSentence.getText().toString();
                                String author = editTextAuthor.getText().toString();
                                String from = editTextFrom.getText().toString();
                                sentenceModel.addSentence(sentence, author, from,
                                        new SentenceModel.OnEditSentenceListener()
                                        {
                                            @Override
                                            public void onResult(boolean success, String message)
                                            {
                                                recyclerViewSentences.post(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        Utils.toast(getContext(), message);
                                                        if (success)
                                                        {
                                                            clearAllSentences();
                                                            loadMoreSentences();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        loadMoreSentences();
    }

    public void loadMoreSentences()
    {
        sentenceModel.getSentences(sentenceAdapter.getItemCount(), COUNT_PER_PAGE,
            new SentenceModel.OnSentencesListener()
            {
                @Override
                public void onSentences(List<Sentence> sentences)
                {
                    if (sentences == null)
                    {
                        recyclerViewSentences.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Utils.toast(getContext(), R.string.get_sentences_failed);
                            }
                        });
                        return;
                    }
                    if (!sentences.isEmpty())
                        sentenceAdapter.addAll(sentences);
                    if (sentences.size() < COUNT_PER_PAGE)
                    {
                        notifySentencesChanged(loadMoreWrapper.LOADING_END);
                        return;
                    }
                    notifySentencesChanged(loadMoreWrapper.LOADING_COMPLETE);
                }
            });
    }

    private void clearAllSentences()
    {
        sentenceAdapter.clear();
        notifySentencesChanged(loadMoreWrapper.LOADING_COMPLETE);
    }

    private void notifySentencesChanged(int loadingState)
    {
        recyclerViewSentences.post(new Runnable()
        {
            @Override
            public void run()
            {
                loadMoreWrapper.setLoadStateNotify(loadingState);
            }
        });
    }
}

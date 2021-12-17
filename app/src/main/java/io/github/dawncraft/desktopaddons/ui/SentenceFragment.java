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

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Sentence;
import io.github.dawncraft.desktopaddons.model.SentenceModel;
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
    private boolean isLoading = false;

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
        sentenceAdapter.setOnSentenceItemListener(new SentenceAdapter.OnSentenceItemListener()
        {
            @Override
            public void onClick(Sentence sentence)
            {
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_sentence_detail, null, false);
                TextView textViewSentence = view.findViewById(R.id.textViewSentence);
                textViewSentence.setText(sentence.getSentence());
                if (sentence.getAuthor() != null)
                {
                    view.findViewById(R.id.tableRowAuthor).setVisibility(View.VISIBLE);
                    TextView textViewAuthor = view.findViewById(R.id.textViewAuthor);
                    textViewAuthor.setText(sentence.getAuthor());
                }
                if (sentence.getFrom() != null)
                {
                    view.findViewById(R.id.tableRowFrom).setVisibility(View.VISIBLE);
                    TextView textViewFrom = view.findViewById(R.id.textViewFrom);
                    textViewFrom.setText(sentence.getFrom());
                }
                new AlertDialog.Builder(requireActivity())
                        .setTitle(getString(R.string.sentence_detail, sentence.getId()))
                        .setView(view)
                        .setNeutralButton(android.R.string.copy, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                StringBuilder sb = new StringBuilder();
                                sb.append(sentence.getSentence());
                                if (sentence.getAuthor() != null)
                                {
                                    sb.append("\n")
                                            .append(getString(R.string.sentence_author))
                                            .append(sentence.getAuthor());
                                }
                                if (sentence.getFrom() != null)
                                {
                                    sb.append("\n")
                                            .append(getString(R.string.sentence_from))
                                            .append(sentence.getFrom());
                                }
                                sb.append("\n")
                                        .append(getString(R.string.divider))
                                        .append("\n")
                                        .append(getString(R.string.sentence_copy_declaration));
                                Utils.copyToClipboard(requireContext(), sb.toString());
                                Utils.toast(requireContext(), R.string.sentence_copy_success);
                            }
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
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
                if (!DAApplication.hasToken())
                {
                    Utils.toast(getContext(), R.string.sentence_need_login);
                    return;
                }
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_add_sentence, null, false);
                EditText editTextSentence = view.findViewById(R.id.editTextSentence);
                EditText editTextAuthor = view.findViewById(R.id.editTextAuthor);
                EditText editTextFrom = view.findViewById(R.id.editTextFrom);
                new AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.add_sentence)
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
                                                if (success)
                                                {
                                                    sentenceAdapter.clear();
                                                    loadMoreSentences();
                                                }
                                                recyclerViewSentences.post(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        Utils.toast(getContext(), message);
                                                    }
                                                });
                                            }
                                        });
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
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
        if (isLoading) return;
        isLoading = true;
        notifySentencesChanged(loadMoreWrapper.LOADING);
        sentenceModel.getSentences(sentenceAdapter.getItemCount(), COUNT_PER_PAGE,
            new SentenceModel.OnSentencesListener()
            {
                @Override
                public void onSentences(List<Sentence> sentences)
                {
                    isLoading = false;
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

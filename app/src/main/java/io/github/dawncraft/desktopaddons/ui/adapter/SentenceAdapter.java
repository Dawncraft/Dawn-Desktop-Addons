package io.github.dawncraft.desktopaddons.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Sentence;

public class SentenceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final List<Sentence> sentences;
    private OnSentenceItemListener listener;

    public SentenceAdapter()
    {
        sentences = Collections.synchronizedList(new ArrayList<>());
    }

    public OnSentenceItemListener getOnSentenceItemListener()
    {
        return listener;
    }

    public void setOnSentenceItemListener(OnSentenceItemListener listener)
    {
        this.listener = listener;
    }

    public void addAll(List<Sentence> list)
    {
        // int index = sentences.size();
        sentences.addAll(list);
        // notifyItemRangeInserted(index, list.size());
    }

    public void clear()
    {
        // int count = sentences.size();
        sentences.clear();
        // notifyItemRangeRemoved(0, count);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sentence, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolder)
        {
            ViewHolder sentenceViewHolder = (ViewHolder) holder;
            Sentence sentence = sentences.get(position);
            String text = String.format("%s. %s", sentence.getId(), sentence.getSentence());
            sentenceViewHolder.getTextViewSentence().setText(text);
            sentenceViewHolder.getTextViewSentence().setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (listener != null) listener.onClick(sentence);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return sentences.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView textViewSentence;

        public ViewHolder(View itemView)
        {
            super(itemView);
            textViewSentence = itemView.findViewById(R.id.textViewSentence);
        }

        public TextView getTextViewSentence()
        {
            return textViewSentence;
        }
    }

    public interface OnSentenceItemListener
    {
        void onClick(Sentence sentence);
    }
}

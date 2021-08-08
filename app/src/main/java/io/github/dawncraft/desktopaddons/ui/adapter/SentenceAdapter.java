package io.github.dawncraft.desktopaddons.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Sentence;

public class SentenceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final List<Sentence> sentences;

    public SentenceAdapter()
    {
        sentences = new ArrayList<>();
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sentence, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolder)
        {
            ViewHolder sentenceViewHolder = (ViewHolder) holder;
            Sentence sentence = sentences.get(position);
            sentenceViewHolder.getTextViewSentence().setText(sentence.getSentence());
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
}

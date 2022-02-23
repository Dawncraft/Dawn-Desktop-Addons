package io.github.dawncraft.desktopaddons.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Wallpaper;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.Holder>
{
    private List<Wallpaper> wallpapers = Collections.emptyList();
    private OnWallpaperItemListener listener;

    public OnWallpaperItemListener getOnWallpaperItemListener()
    {
        return listener;
    }

    public void setOnWallpaperItemListener(OnWallpaperItemListener listener)
    {
        this.listener = listener;
    }

    public void setWallpaperList(List<Wallpaper> list) {
        wallpapers = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallpaper, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position)
    {
        Wallpaper wallpaper = wallpapers.get(position);
        holder.getImageViewThumbnail().setImageResource(R.mipmap.live_wallpaper_preview);
        holder.getTextViewName().setText(wallpaper.name);
        holder.getImageButtonEdit().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener != null) listener.onClick(wallpaper);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return wallpapers.size();
    }

    public static class Holder extends RecyclerView.ViewHolder
    {
        private final ImageView imageViewThumbnail;
        private final TextView textViewName;
        private final ImageButton imageButtonEdit;

        public Holder(View itemView)
        {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageButtonEdit = itemView.findViewById(R.id.imageButtonEdit);
        }

        public ImageView getImageViewThumbnail()
        {
            return imageViewThumbnail;
        }

        public TextView getTextViewName()
        {
            return textViewName;
        }

        public ImageButton getImageButtonEdit()
        {
            return imageButtonEdit;
        }
    }

    public interface OnWallpaperItemListener
    {
        void onClick(Wallpaper wallpaper);
    }
}

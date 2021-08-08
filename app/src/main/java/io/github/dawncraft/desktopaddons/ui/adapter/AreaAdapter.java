package io.github.dawncraft.desktopaddons.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.QQNewsNCPInfo;

public class AreaAdapter extends BaseAdapter
{
    private OnAreaClickListener onAreaClickListener;
    private List<QQNewsNCPInfo> areaList;

    public AreaAdapter()
    {
        areaList = Collections.emptyList();
    }

    public OnAreaClickListener getOnAreaClickListener()
    {
        return onAreaClickListener;
    }

    public void setOnAreaClickListener(OnAreaClickListener listener)
    {
        onAreaClickListener = listener;
    }

    public void setAreaList(List<QQNewsNCPInfo> areas, boolean notify)
    {
        areaList = areas;
        if (notify) notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return areaList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return areaList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_area, parent, false);
        }
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        ImageButton imageButtonSubarea = convertView.findViewById(R.id.imageButtonSubarea);
        QQNewsNCPInfo ncpInfo = (QQNewsNCPInfo) getItem(position);
        textViewName.setText(ncpInfo.getArea());
        if (ncpInfo.getChildren() != null)
        {
            imageButtonSubarea.setVisibility(View.VISIBLE);
            imageButtonSubarea.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (onAreaClickListener != null)
                        onAreaClickListener.onSubareaButtonClick(ncpInfo);
                }
            });
        }
        else
        {
            imageButtonSubarea.setVisibility(View.GONE);
        }
        return convertView;
    }

    public interface OnAreaClickListener
    {
        void onSubareaButtonClick(QQNewsNCPInfo ncpInfo);
    }
}

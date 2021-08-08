package io.github.dawncraft.desktopaddons.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteAdapter<T> extends BaseAdapter implements Filterable
{
    private final Object lock = new Object();
    private final Context context;
    private final LayoutInflater inflater;
    private final int resource;
    private final int fieldId;
    private List<T> objects;
    private ArrayList<T> originalValues;
    private final AutoCompleteFilter filter;

    public AutoCompleteAdapter(Context context, int resource)
    {
        this(context, resource, 0);
    }

    public AutoCompleteAdapter(Context context, int resource, int textViewResourceId)
    {
        this(context, resource, textViewResourceId, new ArrayList<>());
    }

    public AutoCompleteAdapter(Context context, int resource, List<T> objects)
    {
        this(context, resource, 0, objects);
    }

    public AutoCompleteAdapter(Context context, int resource, int textViewResourceId, List<T> objects)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
        this.fieldId = textViewResourceId;
        this.objects = objects;
        this.filter = new AutoCompleteFilter();
    }

    public void setList(List<T> list)
    {
        synchronized (lock)
        {
            objects = list;
            originalValues = null;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return objects.size();
    }

    @Override
    public T getItem(int position)
    {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final View view;
        final TextView text;
        if (convertView == null)
        {
            view = inflater.inflate(resource, parent, false);
        }
        else
        {
            view = convertView;
        }
        try
        {
            if (fieldId == 0)
            {
                text = (TextView) view;
            }
            else
            {
                text = view.findViewById(fieldId);
                if (text == null)
                {
                    throw new RuntimeException("Failed to find view with ID "
                            + context.getResources().getResourceName(fieldId)
                            + " in item layout");
                }
            }
        }
        catch (ClassCastException e)
        {
            Log.e("AutoCompleteAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException("AutoCompleteAdapter requires the resource ID to be a TextView", e);
        }
        final T item = getItem(position);
        if (item instanceof CharSequence)
        {
            text.setText((CharSequence) item);
        }
        else
        {
            text.setText(item.toString());
        }
        return view;
    }

    @Override
    public Filter getFilter()
    {
        return filter;
    }

    private class AutoCompleteFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();
            if (originalValues == null)
            {
                synchronized (lock)
                {
                    originalValues = new ArrayList<>(objects);
                }
            }
            int count = originalValues.size();
            ArrayList<T> values = new ArrayList<>();
            for (int i = 0; i < count; i++)
            {
                T value = originalValues.get(i);
                String valueText = value.toString().toLowerCase();
                if (constraint != null && valueText.contains(constraint))
                {
                    values.add(value);
                }
            }
            results.values = values;
            results.count = values.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            objects = (List<T>) results.values;
            if (results.count > 0)
            {
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }
    }
}

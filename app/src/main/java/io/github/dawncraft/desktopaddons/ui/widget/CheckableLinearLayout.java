package io.github.dawncraft.desktopaddons.ui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable
{
    private boolean checked;

    public CheckableLinearLayout(Context context)
    {
        this(context, null);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    @Override
    public boolean isChecked()
    {
        return checked;
    }

    @Override
    public void toggle()
    {
        setChecked(!checked);
    }
}

package io.github.dawncraft.desktopaddons.ui.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.util.Utils;

public class ComponentSwitchPreference extends SwitchPreferenceCompat
{
    private final boolean mReverse;
    private final ComponentName mComponentName;

    public ComponentSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ComponentSwitchPreference, defStyleAttr, defStyleRes);
        mReverse = a.getBoolean(R.styleable.ComponentSwitchPreference_reverse, false);
        String pkg = a.getString(R.styleable.ComponentSwitchPreference_targetPackage);
        if (pkg == null) pkg = getContext().getPackageName();
        String cls = a.getString(R.styleable.ComponentSwitchPreference_targetClass);
        mComponentName = new ComponentName(pkg, cls);
        a.recycle();
        mChecked = Utils.isComponentEnabled(getContext(), mComponentName);
        if (mReverse) mChecked = !mChecked;
    }

    public ComponentSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public ComponentSwitchPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.switchPreferenceCompatStyle);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        if (enabled == mReverse && isChecked() == !mReverse)
        {
            callChangeListener(mReverse);
            setChecked(mReverse);
        }
    }

    @Override
    public boolean callChangeListener(Object newValue)
    {
        if (newValue instanceof Boolean)
        {
            boolean enable = (Boolean) newValue;
            if (mReverse) enable = !enable;
            Utils.setComponentEnabled(getContext(), mComponentName, enable);
            return true;
        }
        return false;
    }
}

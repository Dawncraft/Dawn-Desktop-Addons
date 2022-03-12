package io.github.dawncraft.desktopaddons.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.IdRes;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;

import io.github.dawncraft.desktopaddons.R;

public class NavigationPreference extends Preference implements Preference.OnPreferenceClickListener
{
    @IdRes
    private int mDestination;

    public NavigationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.NavigationPreference, defStyleAttr, defStyleRes);
        mDestination = a.getResourceId(R.styleable.NavigationPreference_destination, 0);
        a.recycle();
        setOnPreferenceClickListener(this);
    }

    public NavigationPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public NavigationPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public void setDestination(@IdRes int destination)
    {
        mDestination = destination;
    }

    public @IdRes int getDestination()
    {
        return mDestination;
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        Activity activity = (Activity) preference.getContext();
        NavController navController = Navigation.findNavController(activity, R.id.fragmentContainer);
        navController.navigate(getDestination());
        return true;
    }
}

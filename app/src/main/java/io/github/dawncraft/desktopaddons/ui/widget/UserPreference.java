package io.github.dawncraft.desktopaddons.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.preference.Preference;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.model.UserModel;

public class UserPreference extends NavigationPreference
{
    private final Handler handler = new Handler();

    public UserPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public UserPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public UserPreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.preferenceStyle);
    }

    @Override
    public Drawable getIcon()
    {
        return super.getIcon();
    }

    @Override
    public CharSequence getTitle()
    {
        return !DAApplication.hasToken() ? getContext().getString(R.string.user_not_log_in)
                : DAApplication.getPreferences().getString("username", "");
    }

    @Override
    public CharSequence getSummary()
    {
        return getContext().getString(!DAApplication.hasToken() ?
                R.string.user_log_in_summary : R.string.user_logout);
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (DAApplication.hasToken())
        {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.user_logout_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            UserModel userModel = new UserModel();
                            userModel.logout(new Consumer<Boolean>()
                            {
                                @Override
                                public void accept(Boolean success)
                                {
                                    handler.post(UserPreference.this::notifyChanged);
                                }
                            });
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        }
        return super.onPreferenceClick(preference);
    }
}

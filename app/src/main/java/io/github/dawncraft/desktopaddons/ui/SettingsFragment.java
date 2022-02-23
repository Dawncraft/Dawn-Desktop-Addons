package io.github.dawncraft.desktopaddons.ui;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.util.Consumer;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import de.psdev.licensesdialog.LicensesDialog;
import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.model.UserModel;
import io.github.dawncraft.desktopaddons.util.Utils;

public class SettingsFragment extends PreferenceFragmentCompat
{
    private final Handler handler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        SwitchPreferenceCompat preferenceHideAppIcon = findPreference("hide_app_icon");
        if (preferenceHideAppIcon != null)
        {
            ComponentName componentName = new ComponentName(requireContext(), MainActivity.ACTIVITY_ALIAS_NAME);
            preferenceHideAppIcon.setChecked(!Utils.isComponentEnabled(requireContext(), componentName));
            preferenceHideAppIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (newValue instanceof Boolean)
                    {
                        boolean hidden = (boolean) newValue;
                        Utils.setComponentEnabled(requireContext(), componentName, !hidden);
                        return true;
                    }
                    return false;
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Preference preferencePermission = findPreference("notification_policy_access_permission");
            if (preferencePermission != null)
            {
                preferencePermission.setVisible(true);
                preferencePermission.setSummaryProvider(new Preference.SummaryProvider<Preference>()
                {
                    @Override
                    public CharSequence provideSummary(Preference preference)
                    {
                        NotificationManager notificationManager = (NotificationManager)
                                requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        return getString(notificationManager.isNotificationPolicyAccessGranted() ?
                                R.string.has_notification_policy_access_permission :
                                R.string.no_notification_policy_access_permission);
                    }
                });
            }
        }
        Preference preferenceVersion = findPreference("check_update");
        if (preferenceVersion != null)
        {
            PackageInfo packageInfo = Utils.getAppInfo(requireContext());
            preferenceVersion.setSummary(getString(R.string.no_update,
                    packageInfo != null ? packageInfo.versionName : "null"));
            preferenceVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    // TODO 更新检查
                    Utils.toast(SettingsFragment.this.getContext(), "更新检查尚未实现呢~");
                    return true;
                }
            });
        }
        Preference preferenceLicenses = findPreference("view_licenses");
        if (preferenceLicenses != null)
        {
            preferenceLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    showLicenseDialog();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshUserPreference();
    }

    private void refreshUserPreference()
    {
        Preference preferenceUser = findPreference("user");
        if (preferenceUser != null)
        {
            if (DAApplication.hasToken())
            {
                preferenceUser.setTitle(DAApplication.getPreferences().getString("username", ""));
                preferenceUser.setSummary(R.string.user_logout);
                preferenceUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        showLogoutDialog();
                        return true;
                    }
                });
            }
            else
            {
                preferenceUser.setTitle(R.string.user_not_log_in);
                preferenceUser.setSummary(R.string.user_log_in_summary);
                preferenceUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        NavHostFragment.findNavController(SettingsFragment.this)
                                .navigate(R.id.loginFragment);
                        return true;
                    }
                });
            }
        }
    }

    private void showLogoutDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.user_logout_confirm)
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
                                handler.post(SettingsFragment.this::refreshUserPreference);
                            }
                        });
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    private void showLicenseDialog()
    {
        new LicensesDialog.Builder(requireActivity())
                .setNotices(R.raw.licenses)
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }
}

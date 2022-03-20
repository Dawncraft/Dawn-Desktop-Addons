package io.github.dawncraft.desktopaddons.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import de.psdev.licensesdialog.LicensesDialog;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.broadcast.ZenModeBroadcastReceiver;
import io.github.dawncraft.desktopaddons.ui.widget.ComponentSwitchPreference;
import io.github.dawncraft.desktopaddons.util.SystemPropertyUtils;
import io.github.dawncraft.desktopaddons.util.Utils;
import rikka.shizuku.Shizuku;

public class SettingsFragment extends PreferenceFragmentCompat implements Shizuku.OnRequestPermissionResultListener
{
    public static final int PERMISSION_REQUEST_CODE = 233;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Shizuku.addRequestPermissionResultListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        SwitchPreferenceCompat preferenceZenMode = findPreference("zen_mode_switch");
        if (preferenceZenMode != null)
        {
            preferenceZenMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (newValue instanceof Boolean)
                    {
                        if ((Boolean) newValue)
                            ZenModeBroadcastReceiver.register(requireContext());
                        else
                            ZenModeBroadcastReceiver.unregister(requireContext());
                        return true;
                    }
                    return false;
                }
            });
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
                    Utils.toast(getContext(), "更新检查尚未实现呢~");
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
                    new LicensesDialog.Builder(requireActivity())
                            .setNotices(R.raw.licenses)
                            .setIncludeOwnLicense(true)
                            .build()
                            .show();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshShizuku();
        Preference preferencePermission = findPreference("notification_policy_access_permission");
        SwitchPreferenceCompat preferenceZenMode = findPreference("zen_mode_switch");
        if (preferencePermission != null && preferenceZenMode != null)
        {
            boolean flag = Utils.isZenModeGranted(requireContext());
            preferencePermission.setSummary(flag ? R.string.has_notification_policy_access_permission
                    : R.string.no_notification_policy_access_permission);
            preferenceZenMode.setEnabled(flag);
            if (!flag && preferenceZenMode.isChecked())
            {
                ZenModeBroadcastReceiver.unregister(getContext());
                preferenceZenMode.setChecked(false);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(this);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult)
    {
        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            if (grantResult == PackageManager.PERMISSION_GRANTED)
            {
                refreshShizuku();
                Utils.toast(getContext(), R.string.request_permission_successfully);
            }
        }
    }

    private void refreshShizuku()
    {
        Preference preferenceCheckShizuku = findPreference("check_shizuku");
        if (preferenceCheckShizuku != null)
        {
            if (Shizuku.pingBinder())
            {
                int version = Shizuku.getVersion();
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                {
                    preferenceCheckShizuku.setSummary(getString(R.string.shizuku_no_permission, version));
                    preferenceCheckShizuku.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                    {
                        @Override
                        public boolean onPreferenceClick(Preference preference)
                        {
                            if (!Shizuku.shouldShowRequestPermissionRationale())
                            {
                                Shizuku.requestPermission(PERMISSION_REQUEST_CODE);
                                return true;
                            }
                            return false;
                        }
                    });
                }
                else
                {
                    preferenceCheckShizuku.setSummary(getString(R.string.shizuku_has_permission, version));
                    preferenceCheckShizuku.setOnPreferenceClickListener(null);
                }
            }
            else
            {
                preferenceCheckShizuku.setSummary(R.string.shizuku_not_found);
                preferenceCheckShizuku.setOnPreferenceClickListener(null);
            }
        }
        ComponentSwitchPreference preferenceFifthG = findPreference("5g_switch");
        if (preferenceFifthG != null)
        {
            preferenceFifthG.setEnabled(Utils.isFifthGSupported());
        }
        ComponentSwitchPreference preferenceDevTiles = findPreference("dev_tiles_switch");
        if (preferenceDevTiles != null)
        {
            preferenceDevTiles.setEnabled(SystemPropertyUtils.isDevelopmentSettingsEnabled(requireContext()));
        }
    }
}

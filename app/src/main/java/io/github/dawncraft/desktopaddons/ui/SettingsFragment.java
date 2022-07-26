package io.github.dawncraft.desktopaddons.ui;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import de.psdev.licensesdialog.LicensesDialog;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.broadcast.ZenModeBroadcastReceiver;
import io.github.dawncraft.desktopaddons.entity.AppInfo;
import io.github.dawncraft.desktopaddons.model.UpdateModel;
import io.github.dawncraft.desktopaddons.service.DaemonService;
import io.github.dawncraft.desktopaddons.ui.widget.ComponentSwitchPreference;
import io.github.dawncraft.desktopaddons.util.FileUtils;
import io.github.dawncraft.desktopaddons.util.HttpUtils;
import io.github.dawncraft.desktopaddons.util.SystemPropertyUtils;
import io.github.dawncraft.desktopaddons.util.Utils;
import io.github.dawncraft.desktopaddons.worker.NCPInfoWorker;
import rikka.shizuku.Shizuku;

public class SettingsFragment extends PreferenceFragmentCompat
        implements Shizuku.OnRequestPermissionResultListener, UpdateModel.OnAppInfoListener
{
    public static final int PERMISSION_REQUEST_CODE = 233;
    private static final boolean ENABLE_SHIZUKU = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private String version;
    private UpdateModel updateModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (ENABLE_SHIZUKU)
            Shizuku.addRequestPermissionResultListener(this);
        PackageInfo packageInfo = Utils.getAppInfo(requireContext());
        version = packageInfo != null ? packageInfo.versionName : "";
        updateModel = new UpdateModel();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        SwitchPreferenceCompat preferenceKeepAlive = findPreference("keep_alive");
        if (preferenceKeepAlive != null)
        {
            preferenceKeepAlive.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue)
                {
                    if (newValue instanceof Boolean)
                    {
                        if ((Boolean) newValue)
                            DaemonService.startService(getContext());
                        else
                            DaemonService.stopService(getContext());
                        return true;
                    }
                    return false;
                }
            });
        }
        EditTextPreference preferenceUpdateInterval = findPreference("ncp_update_interval");
        if (preferenceUpdateInterval != null)
        {
            preferenceUpdateInterval.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>()
            {
                @Override
                public CharSequence provideSummary(@NonNull EditTextPreference preference)
                {
                    int interval = Integer.parseInt(Objects.requireNonNull(preference.getText()));
                    return interval > 0 ? getString(R.string.ncp_update_interval_unit, interval)
                            : getString(R.string.ncp_update_interval_manual);
                }
            });
            preferenceUpdateInterval.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener()
            {
                @Override
                public void onBindEditText(@NonNull EditText editText)
                {
                    editText.setHint(R.string.ncp_update_interval_edit_hint);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            });
            preferenceUpdateInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue)
                {
                    if (newValue instanceof String)
                    {
                        try
                        {
                            int interval = Integer.parseInt((String) newValue);
                            NCPInfoWorker.stopAllWorks(getContext());
                            NCPInfoWorker.startSyncWork(getContext(), interval);
                            return true;
                        }
                        catch (NumberFormatException ignored) {}
                    }
                    return false;
                }
            });
        }
        Preference preferenceZenPermission = findPreference("notification_policy_access_permission");
        if (preferenceZenPermission != null)
        {
            preferenceZenPermission.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
        }
        SwitchPreferenceCompat preferenceZenMode = findPreference("zen_mode_switch");
        if (preferenceZenMode != null)
        {
            preferenceZenMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue)
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
            preferenceVersion.setSummary(getString(R.string.version, version)
                    + " " + getString(R.string.checking_update));
            preferenceVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference)
                {
                    preferenceVersion.setTitle(R.string.check_update);
                    preference.setSummary(getString(R.string.version, version)
                            + " " + getString(R.string.checking_update));
                    updateModel.checkUpdate(false, SettingsFragment.this);
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
                public boolean onPreferenceClick(@NonNull Preference preference)
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
        updateModel.checkUpdate(true, this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (ENABLE_SHIZUKU)
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
            if (!ENABLE_SHIZUKU)
            {
                preferenceCheckShizuku.setEnabled(false);
                preferenceCheckShizuku.setSummary(R.string.shizuku_not_support);
            }
            else if (Shizuku.pingBinder())
            {
                int version = Shizuku.getVersion();
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                {
                    preferenceCheckShizuku.setSummary(getString(R.string.shizuku_no_permission, version));
                    preferenceCheckShizuku.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                    {
                        @Override
                        public boolean onPreferenceClick(@NonNull Preference preference)
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
        boolean isTileSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        ComponentSwitchPreference preferenceFifthG = findPreference("5g_switch");
        if (preferenceFifthG != null)
        {
            preferenceFifthG.setEnabled(isTileSupported && Utils.isFifthGSupported());
        }
        ComponentSwitchPreference preferenceDevTiles = findPreference("dev_tiles_switch");
        if (preferenceDevTiles != null)
        {
            preferenceDevTiles.setEnabled(isTileSupported &&
                    SystemPropertyUtils.isDevelopmentSettingsEnabled(requireContext()));
        }
    }

    @Override
    public void onResponse(UpdateModel.Result result, AppInfo appInfo)
    {
        requireActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Preference preferenceVersion = findPreference("check_update");
                if (result == UpdateModel.Result.BUSY)
                {
                    Utils.toast(getContext(), R.string.checking_update);
                }
                else if (result == UpdateModel.Result.ERROR)
                {
                    if (preferenceVersion != null)
                    {
                        preferenceVersion.setSummary(getString(R.string.version, version)
                                + " " + getString(R.string.update_error));
                    }
                    Utils.toast(getContext(), R.string.update_error);
                }
                else
                {
                    // TODO 按照语义化版本进行比较, 或者按版本号?
                    if (!version.equals(appInfo.getVersion()))
                    {
                        if (preferenceVersion != null)
                        {
                            preferenceVersion.setTitle(R.string.get_update);
                            preferenceVersion.setSummary(getString(R.string.version, version)
                                    + " " + getString(R.string.has_update, appInfo.getVersion()));
                            preferenceVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                            {
                                @Override
                                public boolean onPreferenceClick(@NonNull Preference preference)
                                {
                                    String message = getString(R.string.update_dialog_time, appInfo.getReleaseTime()) +
                                            "\n" +
                                            getString(R.string.update_dialog_size, FileUtils.getFormatSize(appInfo.getSize())) +
                                            "\n" +
                                            getString(R.string.update_dialog_message) +
                                            "\n" +
                                            appInfo.getMessage();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                                            .setTitle(getString(R.string.has_update, appInfo.getVersion()))
                                            .setMessage(message)
                                            .setPositiveButton(R.string.update_dialog_download, new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    HttpUtils.openUrl(getContext(), appInfo.getDownloadUrl(), false);
                                                }
                                            })
                                            .setNegativeButton(R.string.update_dialog_ignore, null);
                                    builder.show();
                                    return true;
                                }
                            });
                        }
                        Utils.toast(getContext(), R.string.notify_update);
                    }
                    else
                    {
                        if (preferenceVersion != null)
                        {
                            preferenceVersion.setSummary(getString(R.string.version, version)
                                    + " " + getString(R.string.no_update));
                        }
                    }
                }
            }
        });
    }
}

package io.github.dawncraft.desktopaddons;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                Preference permissionPreference = findPreference("permission");
                if (permissionPreference != null)
                {
                    permissionPreference.setVisible(true);
                    permissionPreference.setSummaryProvider(new Preference.SummaryProvider<Preference>()
                    {
                        @Override
                        public CharSequence provideSummary(Preference preference)
                        {
                            NotificationManager notificationManager = (NotificationManager)
                                    SettingsFragment.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager.isNotificationPolicyAccessGranted())
                                return "您已授予修改勿扰模式权限";
                            else
                                return "该功能需要手动授予修改勿扰模式的权限";
                        }
                    });
                }
            }
            Preference versionPreference = findPreference("version");
            if (versionPreference != null)
            {
                versionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        Utils.toast(SettingsFragment.this.getContext(), "说了没写呢, 你点啥");
                        return true;
                    }
                });
            }
        }
    }
}

package io.github.dawncraft.desktopaddons.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.dawncraft.desktopaddons.R;

public class MainActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
    public static final String ACTIVITY_ALIAS_NAME = "io.github.dawncraft.desktopaddons.ui.MainActivityAlias";
    private static final Map<String, Integer> NAVIGATION_ID_MAP = new HashMap<>();
    static
    {
        NAVIGATION_ID_MAP.put(WallpaperFragment.class.getName(), R.id.wallpaperFragment);
        NAVIGATION_ID_MAP.put(SentenceFragment.class.getName(), R.id.sentenceFragment);
    }
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        navController = Objects.requireNonNull(navHostFragment).getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (super.onOptionsItemSelected(item)) return true;
        if (item.getItemId() == android.R.id.home)
        {
            navController.navigateUp();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref)
    {
        Integer destination = NAVIGATION_ID_MAP.get(pref.getFragment());
        if (destination != null)
        {
            navController.navigate(destination);
            return true;
        }
        return false;
    }
}

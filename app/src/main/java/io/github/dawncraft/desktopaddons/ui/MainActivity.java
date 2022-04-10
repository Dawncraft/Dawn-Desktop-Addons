package io.github.dawncraft.desktopaddons.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.service.DaemonService;

public class MainActivity extends AppCompatActivity
{
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
        // NOTE Android 12 后不允许在后台启动前台服务, 所以在MainActivity中启动
        DaemonService.startService(getApplicationContext());
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}

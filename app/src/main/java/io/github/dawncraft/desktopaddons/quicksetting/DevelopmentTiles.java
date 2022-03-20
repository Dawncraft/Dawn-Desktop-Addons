package io.github.dawncraft.desktopaddons.quicksetting;

import android.os.Build;

import androidx.annotation.RequiresApi;

import io.github.dawncraft.desktopaddons.util.SystemPropertyUtils;

/**
 * 自己实现的快捷设置开发者图块, 因为有些系统(如华为)阉割了这一功能
 *
 * @author QingChenW
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class DevelopmentTiles extends SwitchTile
{
    private DevelopmentTiles() {}

    @Override
    public void onClick()
    {
        super.onClick();
        SystemPropertyUtils.pokeSystemProps();
    }

    @Override
    protected boolean isAvailable()
    {
        return SystemPropertyUtils.isDevelopmentSettingsEnabled(this);
    }

    public static class ShowLayout extends DevelopmentTiles
    {
        @Override
        protected boolean isEnabled()
        {
            String property = SystemPropertyUtils.getProperty("debug.layout", "false");
            return Boolean.parseBoolean(property);
        }

        @Override
        protected void setEnabled(boolean enabled)
        {
            SystemPropertyUtils.setProperty("debug.layout", enabled ? "true" : "false");
        }
    }
}

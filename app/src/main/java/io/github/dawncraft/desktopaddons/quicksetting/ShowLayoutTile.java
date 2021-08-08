package io.github.dawncraft.desktopaddons.quicksetting;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import io.github.dawncraft.desktopaddons.util.Utils;

/**
 * 开启布局边界显示的快捷开关
 *
 * @author QingChenW
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class ShowLayoutTile extends TileService
{
    private static final String TAG = "ShowLayoutTile";

    @Override
    public void onStartListening()
    {
        Log.d(TAG, "onStartListening");
        // NOTE 在设置中没找到显示布局边界
        // Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED)
        String property = Utils.getProperty("debug.layout");
        if (property != null)
        {
            getQsTile().setState(Boolean.parseBoolean(property) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        else
        {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
        }
        getQsTile().updateTile();
    }

    @Override
    public void onClick()
    {
        Log.d(TAG, "onClick");
        int state = getQsTile().getState();
        if (state == Tile.STATE_INACTIVE)
        {
            Utils.setProperty("debug.layout", "true");
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        else if (state == Tile.STATE_ACTIVE)
        {
            Utils.setProperty("debug.layout", "false");
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public void onStopListening()
    {
        Log.d(TAG, "onStopListening");
    }
}

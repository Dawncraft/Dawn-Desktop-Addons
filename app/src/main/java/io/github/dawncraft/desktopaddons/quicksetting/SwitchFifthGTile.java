package io.github.dawncraft.desktopaddons.quicksetting;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import io.github.dawncraft.desktopaddons.util.Utils;

/**
 * 切换5G开关的快捷开关
 *
 * @author QingChenW
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class SwitchFifthGTile extends TileService
{
    private static final String TAG = "ShowLayoutTile";

    @Override
    public void onStartListening()
    {
        Log.d(TAG, "onStartListening");
        if (Utils.isFifthGSupported())
        {
            getQsTile().setState(Utils.isFifthGEnabled() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
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
        if (!Utils.isFifthGSupported())
            return;
        boolean isEnabled = Utils.isFifthGEnabled();
        Utils.setFifthGEnabled(!isEnabled);
        getQsTile().setState(!isEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    @Override
    public void onStopListening()
    {
        Log.d(TAG, "onStopListening");
    }
}

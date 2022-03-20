package io.github.dawncraft.desktopaddons.quicksetting;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

/**
 * 行为是开关的快捷方式图块
 *
 * @author QingChenW
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class SwitchTile extends TileService
{
    @Override
    public void onStartListening()
    {
        refresh();
    }

    @Override
    public void onClick()
    {
        setEnabled(getQsTile().getState() == Tile.STATE_INACTIVE);
        refresh();
    }

    private void refresh()
    {
        int state;
        if (isAvailable())
        {
            state = isEnabled() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
        }
        else
        {
            state = Tile.STATE_UNAVAILABLE;
        }
        getQsTile().setState(state);
        getQsTile().updateTile();
    }

    protected abstract boolean isAvailable();
    protected abstract boolean isEnabled();
    protected abstract void setEnabled(boolean enabled);
}

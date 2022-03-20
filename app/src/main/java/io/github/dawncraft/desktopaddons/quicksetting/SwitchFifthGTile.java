package io.github.dawncraft.desktopaddons.quicksetting;

import android.os.Build;

import androidx.annotation.RequiresApi;

import io.github.dawncraft.desktopaddons.util.Utils;

/**
 * 切换5G开关的快捷方式图块
 *
 * @author QingChenW
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class SwitchFifthGTile extends SwitchTile
{
    @Override
    protected boolean isAvailable()
    {
        return Utils.isFifthGSupported();
    }

    @Override
    protected boolean isEnabled()
    {
        return Utils.isFifthGEnabled();
    }

    @Override
    protected void setEnabled(boolean enabled)
    {
        Utils.setFifthGEnabled(enabled);
    }
}

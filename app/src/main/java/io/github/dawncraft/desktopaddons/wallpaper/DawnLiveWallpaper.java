package io.github.dawncraft.desktopaddons.wallpaper;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.entity.Wallpaper;
import io.github.dawncraft.desktopaddons.ui.LiveWallpaperActivity;
import io.github.dawncraft.desktopaddons.wallpaper.model.ModelManager;
import site.hanschen.glwallpaperservice.GLWallpaperService;

public class DawnLiveWallpaper extends GLWallpaperService
{
    private Wallpaper wallpaper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        int id = DAApplication.getPreferences().getInt("wallpaper_id", 0);
        wallpaper = LiveWallpaperActivity.FAKE_WALLPAPERS.get(id);
    }

    @Override
    protected GLEngine createGLEngine()
    {
        return new GLEngine()
        {
            private WallpaperRenderer renderer;

            @Override
            protected void setupGLSurfaceView(boolean isPreview)
            {
                // if (GLUtils.isSupportGL20(getApplicationContext()))
                //     setEGLContextClientVersion(2);
                // setEGLConfigChooser(new EglConfigChooser(8, 8, 8, 0, 0, 0, 0));
                ModelManager modelManager = new ModelManager(getApplicationContext());
                renderer = new WallpaperRenderer(wallpaper, modelManager);
                setRenderer(renderer);
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }

            @Override
            public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
            {
                super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            }

            @Override
            public void onTouchEvent(MotionEvent event)
            {
                super.onTouchEvent(event);
                renderer.onTouchEvent(event);
            }

            @Override
            public void onDestroy()
            {
                super.onDestroy();
                if (renderer != null)
                    renderer.release();
                renderer = null;
            }
        };
    }

    // WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
    @SuppressLint("ObsoleteSdkInt")
    public static void openLiveWallpaperPreview(Context context)
    {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(context, DawnLiveWallpaper.class));
        }
        else
        {
            intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        }
        context.startActivity(intent);
    }
}

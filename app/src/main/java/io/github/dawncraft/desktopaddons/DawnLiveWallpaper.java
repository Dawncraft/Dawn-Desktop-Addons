package io.github.dawncraft.desktopaddons;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class DawnLiveWallpaper extends WallpaperService
{
    @Override
    public Engine onCreateEngine()
    {
        return new DawnEngine();
    }

    class DawnEngine extends Engine
    {
        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder)
        {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder)
        {
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
        {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }

        @Override
        public SurfaceHolder getSurfaceHolder()
        {
            return super.getSurfaceHolder();
        }
    }
}

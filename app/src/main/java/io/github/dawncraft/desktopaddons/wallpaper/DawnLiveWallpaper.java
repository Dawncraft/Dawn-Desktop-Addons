package io.github.dawncraft.desktopaddons.wallpaper;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.dawncraft.desktopaddons.util.GLUtils;
import site.hanschen.glwallpaperservice.EglConfigChooser;
import site.hanschen.glwallpaperservice.GLWallpaperService;

public class DawnLiveWallpaper extends GLWallpaperService
{
    @Override
    protected GLEngine createGLEngine()
    {
        return new GLEngine()
        {
            @Override
            protected void setupGLSurfaceView(boolean isPreview)
            {
                if (GLUtils.isSupportGL20(DawnLiveWallpaper.this))
                {
                    setEGLContextClientVersion(2);
                }
                setEGLConfigChooser(new EglConfigChooser(8, 8, 8, 0, 0, 0, 0));
                setRenderer(new WallpaperRenderer());
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }

            @Override
            public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
            {
                super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            }
        };
    }

    public static class WallpaperRenderer implements GLSurfaceView.Renderer
    {
        public WallpaperRenderer() {}

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl)
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }
}

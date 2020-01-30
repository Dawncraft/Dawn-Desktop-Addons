package io.github.dawncraft.desktopaddons;

import android.opengl.GLES20;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import site.hanschen.glwallpaperservice.GLWallpaperService;

public class DawnLiveWallpaper extends GLWallpaperService
{
    @Override
    public Engine onCreateEngine()
    {
        return new DawnEngine(new DawnRenderer());
    }

    public class DawnEngine extends GLEngine
    {
        private Renderer renderer;

        public DawnEngine(Renderer renderer)
        {
            super();
            this.renderer = renderer;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);
            if (GLUtils.isSupportGL20(DawnLiveWallpaper.this))
            {
                setEGLContextClientVersion(2);
                setRenderer(renderer);
            }
            else
            {
                return;
            }
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            if (renderer != null)
            {
                renderer.release();
            }
            renderer = null;
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
        {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            renderer.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
        }
    }

    public interface Renderer extends GLWallpaperService.Renderer
    {
        void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset);
        void release();
    }

    public class DawnRenderer implements Renderer
    {
        public DawnRenderer() {}

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

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
        {

        }

        @Override
        public void release() {}
    }
}

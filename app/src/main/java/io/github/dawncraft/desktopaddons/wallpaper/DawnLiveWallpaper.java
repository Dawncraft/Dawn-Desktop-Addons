package io.github.dawncraft.desktopaddons.wallpaper;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.dawncraft.desktopaddons.util.GLUtils;
import site.hanschen.glwallpaperservice.GLWallpaperService;

public class DawnLiveWallpaper extends GLWallpaperService
{
    @Override
    protected GLEngine createGLEngine()
    {
        return new GLEngine()
        {
            private WallpaperRenderer renderer;

            @Override
            protected void setupGLSurfaceView(boolean isPreview)
            {
                if (GLUtils.isSupportGL20(getApplicationContext()))
                {
                    setEGLContextClientVersion(2);
                }
                //setEGLConfigChooser(new EglConfigChooser(8, 8, 8, 0, 0, 0, 0));
                renderer = new WallpaperRenderer(new ModelManager(getApplicationContext()));
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
                {
                    renderer.release();
                }
                renderer = null;
            }
        };
    }

    public static class WallpaperRenderer implements GLSurfaceView.Renderer
    {
        private ModelManager modelManager;
        private Live2DRenderer live2DRenderer;

        public WallpaperRenderer(ModelManager modelMgr)
        {
            modelManager = modelMgr;
            live2DRenderer = new Live2DRenderer();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            live2DRenderer.onSurfaceCreated(gl, config);
            live2DRenderer.setModel(
                    modelManager.loadLive2DModel("ftq_xhjy/model.moc",
                            new String[] { "ftq_xhjy/texture_00.png", "ftq_xhjy/texture_01.png" }),
                    modelManager.loadLive2DMotion("ftq_xhjy/action/idle.mtn"),
                    modelManager.loadLive2DPhysics("ftq_xhjy/moc/physics.json")
            );
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            live2DRenderer.onSurfaceChanged(gl, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl)
        {
            live2DRenderer.onDrawFrame(gl);
        }

        public void onTouchEvent(MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_HOVER_MOVE:
                    live2DRenderer.drag(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    live2DRenderer.resetDrag();
                    break;
            }
        }

        public void release()
        {
            modelManager = null;
            live2DRenderer = null;
        }
    }
}

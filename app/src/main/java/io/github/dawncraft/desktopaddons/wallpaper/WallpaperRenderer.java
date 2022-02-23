package io.github.dawncraft.desktopaddons.wallpaper;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.dawncraft.desktopaddons.entity.Wallpaper;
import io.github.dawncraft.desktopaddons.wallpaper.model.Live2DModel;
import io.github.dawncraft.desktopaddons.wallpaper.model.Model;
import io.github.dawncraft.desktopaddons.wallpaper.model.ModelManager;

public class WallpaperRenderer implements GLSurfaceView.Renderer
{
    private Wallpaper wallpaper;
    private ModelManager modelManager;
    private final List<Model> models = new ArrayList<>();
    private boolean isLoaded = false;

    public WallpaperRenderer(Wallpaper wallpaper, ModelManager modelManager)
    {
        this.wallpaper = wallpaper;
        this.modelManager = modelManager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        ModelManager.bindGL(gl);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (!isLoaded)
        {
            isLoaded = true;
            Live2DModel live2DModel = new Live2DModel();
            live2DModel.setModel(
                    modelManager.loadLive2DModel(wallpaper.modelPath, wallpaper.texturePaths),
                    modelManager.loadLive2DMotion(wallpaper.motionPaths[0]),
                    wallpaper.physicsPath != null ? modelManager.loadLive2DPhysics(wallpaper.physicsPath) : null
            );
            models.add(live2DModel);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0, 0, width, height);
        for (Model model : models)
            model.reshape(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        for (Model model : models)
        {
            gl.glPushMatrix();
            model.render(gl);
            gl.glPopMatrix();
        }
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                for (Model model : models)
                    model.drag(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (Model model : models)
                    model.resetDrag();
                break;
        }
    }

    public void release()
    {
        modelManager = null;
        wallpaper = null;
        models.clear();
    }
}

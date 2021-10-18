package io.github.dawncraft.desktopaddons.wallpaper;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.framework.L2DPhysics;
import jp.live2d.framework.L2DStandardID;
import jp.live2d.framework.L2DTargetPoint;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;

public class Live2DRenderer implements GLSurfaceView.Renderer
{
    private Live2DModelAndroid l2dModel;
    private Live2DMotion l2dMotion;
    private L2DPhysics l2dPhysics;
    private final MotionQueueManager motionManager;
    private final L2DTargetPoint dragManager;
    private float glWidth;
    private float glHeight;

    public Live2DRenderer()
    {
        motionManager = new MotionQueueManager();
        dragManager = new L2DTargetPoint();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        ModelManager.bindGL(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float modelWidth = l2dModel.getCanvasWidth();
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(
                0,
                modelWidth,
                modelWidth / ratio,
                0,
                0.5f, -0.5f
        );
        glWidth = width;
        glHeight = height;
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
        l2dModel.loadParam();
        if (motionManager.isFinished())
        {
            motionManager.startMotion(l2dMotion, false);
        }
        else
        {
            motionManager.updateParam(l2dModel);
        }
        l2dModel.saveParam();
        dragManager.update();
        float dragX = dragManager.getX();
        float dragY = dragManager.getY();
        l2dModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_X, dragX * 30);
        l2dModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Y, dragY * 30);
        l2dModel.addToParamFloat(L2DStandardID.PARAM_BODY_ANGLE_X, dragX * 10);
        if (l2dPhysics != null)
            l2dPhysics.updateParam(l2dModel);
        l2dModel.setGL(gl);
        l2dModel.update();
        l2dModel.draw();
    }

    public void setModel(Live2DModelAndroid model, Live2DMotion motion, L2DPhysics physics)
    {
        l2dModel = model;
        l2dMotion = motion;
        l2dPhysics = physics;
    }

    public void drag(float x, float y)
    {
        // int[] viewport = new int[4];
        // GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
        float screenX = x / glWidth * 2 - 1;
        float screenY = -y / glHeight * 2 + 1;
        dragManager.set(screenX, screenY);
    }

    public void resetDrag()
    {
        dragManager.set(0, 0);
    }
}

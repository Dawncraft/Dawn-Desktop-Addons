package io.github.dawncraft.desktopaddons.wallpaper.model;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.framework.L2DPhysics;
import jp.live2d.framework.L2DStandardID;
import jp.live2d.framework.L2DTargetPoint;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;

public class Live2DModel extends Model
{
    private Live2DModelAndroid l2dModel;
    private Live2DMotion l2dMotion;
    private L2DPhysics l2dPhysics;
    private final MotionQueueManager motionManager;
    private final L2DTargetPoint dragManager;
    private float glWidth;
    private float glHeight;

    public Live2DModel()
    {
        motionManager = new MotionQueueManager();
        dragManager = new L2DTargetPoint();
    }

    @Override
    public void reshape(GL10 gl, int width, int height)
    {
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
    public void render(GL10 gl)
    {
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
        float modelHeight = l2dModel.getCanvasHeight();
        float ratio = glWidth / l2dModel.getCanvasWidth();
        // gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(0.0f, Math.max(glHeight / ratio - modelHeight, 0), 0.0f);
        l2dModel.draw();
    }

    @Override
    public void drag(float x, float y)
    {
        // int[] viewport = new int[4];
        // GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
        float screenX = x / glWidth * 2 - 1;
        float screenY = -y / glHeight * 2 + 1;
        dragManager.set(screenX, screenY);
    }

    @Override
    public void resetDrag()
    {
        dragManager.set(0, 0);
    }

    public void setModel(Live2DModelAndroid model, Live2DMotion motion, L2DPhysics physics)
    {
        l2dModel = model;
        l2dMotion = motion;
        l2dPhysics = physics;
    }
}

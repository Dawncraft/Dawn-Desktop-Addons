package io.github.dawncraft.desktopaddons.wallpaper;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.framework.L2DPhysics;
import jp.live2d.motion.Live2DMotion;

public class ModelManager
{
    private static GL10 gl;
    private final Context context;
    private final AssetManager assetManager;

    public ModelManager(Context ctx)
    {
        context = ctx;
        assetManager = ctx.getAssets();
    }

    public static void bindGL(GL10 gl)
    {
        ModelManager.gl = gl;
    }

    public Live2DModelAndroid loadLive2DModel(String modelPath, String[] texturePaths)
    {
        try (InputStream in = assetManager.open(modelPath))
        {
            Live2DModelAndroid model = Live2DModelAndroid.loadModel(in);
            for (int i = 0; i < texturePaths.length; i++)
            {
                try (InputStream in2 = assetManager.open(texturePaths[i]))
                {
                    int texNo = UtOpenGL.loadTexture(gl, in2, false);
                    model.setTexture(i, texNo);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
            return model;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Live2DMotion loadLive2DMotion(String motionPath)
    {
        try (InputStream in = assetManager.open(motionPath))
        {
            return Live2DMotion.loadMotion(in);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public L2DPhysics loadLive2DPhysics(String physicsPath)
    {
        try (InputStream in = assetManager.open(physicsPath))
        {
            return L2DPhysics.load(in);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}

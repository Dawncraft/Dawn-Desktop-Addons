package io.github.dawncraft.desktopaddons;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLUtils
{
    private static final String TAG = "GLUtils";
    public static final int SIZEOF_FLOAT = 4;

    public static boolean isSupportGL20(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null)
        {
            return false;
        }
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    public static FloatBuffer createFloatBuffer(float[] coords)
    {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * create a vertex shader type (GLES20.GL_VERTEX_SHADER)
     * or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
     *
     * @param type
     * @param shaderCode
     * @return
     */
    public static int createShader(int type, String shaderCode)
    {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        // add the source code to the shader and compile it
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0)
        {
            Log.e(TAG, "compile shader: " + type + ", error: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static int createVertexShader(String shaderCode)
    {
        return createShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int createFragmentShader(String shaderCode)
    {
        return createShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int createProgram(int vertexShader, int fragmentShader)
    {
        if (vertexShader == 0 || fragmentShader == 0)
        {
            Log.e(TAG, "shader can't be 0!");
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0)
        {
            Log.e(TAG, "program can't be 0!");
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE)
        {
            Log.e(TAG, "link program error: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private static void checkGlError(String op)
    {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, "checkGlError: " + msg);
        }
    }
}

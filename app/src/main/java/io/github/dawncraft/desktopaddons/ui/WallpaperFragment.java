package io.github.dawncraft.desktopaddons.ui;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.wallpaper.Live2DRenderer;
import io.github.dawncraft.desktopaddons.wallpaper.ModelManager;

public class WallpaperFragment extends Fragment
{
    private GLSurfaceView surfaceView;
    private ModelManager modelManager;
    private Live2DRenderer renderer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        surfaceView = root.findViewById(R.id.surfaceView);
        surfaceView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        renderer.drag(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        renderer.resetDrag();
                        break;
                }
                return true;
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        modelManager = new ModelManager(requireContext());
        renderer = new Live2DRenderer();
        surfaceView.setRenderer(new GLSurfaceView.Renderer()
        {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config)
            {
                renderer.onSurfaceCreated(gl, config);
                renderer.setModel(
                        modelManager.loadLive2DModel("ftq_xhjy/model.moc",
                                new String[] { "ftq_xhjy/texture_00.png", "ftq_xhjy/texture_01.png" }),
                        modelManager.loadLive2DMotion("ftq_xhjy/action/idle.mtn"),
                        modelManager.loadLive2DPhysics("ftq_xhjy/moc/physics.json")
                );
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height)
            {
                renderer.onSurfaceChanged(gl, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl)
            {
                renderer.onDrawFrame(gl);
            }
        });
    }
}

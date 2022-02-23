package io.github.dawncraft.desktopaddons.ui;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Wallpaper;
import io.github.dawncraft.desktopaddons.util.Utils;
import io.github.dawncraft.desktopaddons.wallpaper.DawnLiveWallpaper;
import io.github.dawncraft.desktopaddons.wallpaper.WallpaperRenderer;
import io.github.dawncraft.desktopaddons.wallpaper.model.ModelManager;

public class WallpaperEditorFragment extends Fragment
{
    private GLSurfaceView surfaceView;
    private ModelManager modelManager;
    private WallpaperRenderer renderer;

    private Wallpaper wallpaper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_wallpaper_editor, container, false);
        surfaceView = root.findViewById(R.id.surfaceView);
        surfaceView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (renderer != null)
                    renderer.onTouchEvent(event);
                return true;
            }
        });
        Button buttonApply = root.findViewById(R.id.buttonApply);
        buttonApply.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DAApplication.getPreferences().edit().putInt("wallpaper_id", wallpaper.id).apply();
                Utils.openLiveWallpaperPreview(getContext(), DawnLiveWallpaper.class);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        int id = requireArguments().getInt("wallpaper_id");
        wallpaper = LiveWallpaperActivity.FAKE_WALLPAPERS.get(id);
        modelManager = new ModelManager(requireContext());
        renderer = new WallpaperRenderer(wallpaper, modelManager);
        surfaceView.setRenderer(renderer);
    }
}

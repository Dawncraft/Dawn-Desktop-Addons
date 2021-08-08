package io.github.dawncraft.desktopaddons.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.github.dawncraft.desktopaddons.R;

public class WallpaperFragment extends Fragment
{
    private SurfaceView surfaceView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        surfaceView = root.findViewById(R.id.surfaceView);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }
}

package io.github.dawncraft.desktopaddons.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.entity.Wallpaper;
import io.github.dawncraft.desktopaddons.ui.adapter.WallpaperAdapter;
import io.github.dawncraft.desktopaddons.util.Utils;

public class LiveWallpaperActivity extends AppCompatActivity
{
    public static final List<Wallpaper> FAKE_WALLPAPERS = new ArrayList<>();
    static
    {
        // TODO 动态壁纸测试
        Wallpaper temp = new Wallpaper();
        temp.id = 0;
        temp.name = "佛跳墙";
        temp.modelPath = "ftq_xhjy/model.moc";
        temp.texturePaths = new String[] { "ftq_xhjy/texture_00.png", "ftq_xhjy/texture_01.png" };
        temp.motionPaths = new String[] { "ftq_xhjy/action/idle.mtn" };
        // temp.physicsPath = "ftq_xhjy/moc/physics.json";
        FAKE_WALLPAPERS.add(temp);
        temp = new Wallpaper();
        temp.id = 1;
        temp.name = "怀抱鲤";
        temp.modelPath = "hbl/model.moc";
        temp.texturePaths = new String[] { "hbl/texture_00.png", "hbl/texture_01.png", "hbl/texture_02.png", "hbl/texture_03.png" };
        temp.motionPaths = new String[] { "hbl/action/idle.mtn" };
        FAKE_WALLPAPERS.add(temp);
        temp = new Wallpaper();
        temp.id = 2;
        temp.name = "锅包肉";
        temp.modelPath = "gbr/model.moc";
        temp.texturePaths = new String[] { "gbr/texture_00.png" };
        temp.motionPaths = new String[] { "gbr/action/idle.mtn" };
        temp.physicsPath = "gbr/moc/physics.json";
        FAKE_WALLPAPERS.add(temp);
    }

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_wallpaper);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        navController = Objects.requireNonNull(navHostFragment).getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder()
                // .setFallbackOnNavigateUpListener(this::onSupportNavigateUp)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public static class LiveWallpaperFragment extends Fragment
    {
        private RecyclerView recyclerViewWallpapers;
        private WallpaperAdapter wallpaperAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // 初始化Model
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View root = inflater.inflate(R.layout.fragment_live_wallpaper, container, false);
            wallpaperAdapter = new WallpaperAdapter();
            wallpaperAdapter.setWallpaperList(FAKE_WALLPAPERS);
            wallpaperAdapter.setOnWallpaperItemListener(new WallpaperAdapter.OnWallpaperItemListener()
            {
                @Override
                public void onClick(Wallpaper wallpaper)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt("wallpaper_id", wallpaper.id);
                    NavHostFragment.findNavController(LiveWallpaperFragment.this)
                            .navigate(R.id.wallpaperEditorFragment, bundle);
                }
            });
            recyclerViewWallpapers = root.findViewById(R.id.recyclerViewWallpapers);
            // recyclerViewWallpapers.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerViewWallpapers.setAdapter(wallpaperAdapter);
            FloatingActionButton buttonAdd = root.findViewById(R.id.floatingActionButtonAdd);
            buttonAdd.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Utils.toast(getContext(), "点击了添加壁纸按钮");
                }
            });
            return root;
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
        {
            super.onViewCreated(view, savedInstanceState);
            // 读取数据
        }
    }
}

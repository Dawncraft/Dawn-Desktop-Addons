package io.github.dawncraft.desktopaddons.wallpaper.model;

import javax.microedition.khronos.opengles.GL10;

public abstract class Model
{
    public abstract void reshape(GL10 gl, int width, int height);

    public abstract void render(GL10 gl);

    public void drag(float x, float y) {}

    public void resetDrag() {}
}

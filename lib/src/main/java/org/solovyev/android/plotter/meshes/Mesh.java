package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public interface Mesh {

    boolean init();

    boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config);

    void draw(@Nonnull GL11 gl);

    @Nonnull
    Mesh copy();

    @Nonnull
    State getState();

    void setAlpha(float alpha);

    boolean setColor(@Nonnull Color color);

    @Nonnull
    Color getColor();

    boolean setWidth(int width);

    int getWidth();

    enum State {
        DIRTY(0),
        INITIALIZING(1),
        INIT(2),
        INITIALIZING_GL(3),
        INIT_GL(4);

        final int order;

        State(int order) {
            this.order = order;
        }
    }
}

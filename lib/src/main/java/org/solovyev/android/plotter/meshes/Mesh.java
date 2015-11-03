package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public interface Mesh {

    boolean init();

    boolean initGl(@NonNull GL11 gl, @NonNull MeshConfig config);

    void draw(@NonNull GL11 gl);

    @NonNull
    Mesh copy();

    @NonNull
    State getState();

    void setAlpha(float alpha);

    boolean setColor(@NonNull Color color);

    @NonNull
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

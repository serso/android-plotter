package org.solovyev.android.plotter.meshes;

import javax.microedition.khronos.opengles.GL10;

public enum IndicesOrder {
    TRIANGLES(GL10.GL_TRIANGLES),
    TRIANGLE_STRIP(GL10.GL_TRIANGLE_STRIP),
    LINE_STRIP(GL10.GL_LINE_STRIP),
    LINE_LOOP(GL10.GL_LINE_LOOP),
    LINES(GL10.GL_LINES),
    POINTS(GL10.GL_POINTS);

    public final int glMode;

    IndicesOrder(int glMode) {
        this.glMode = glMode;
    }
}

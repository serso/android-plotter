package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public class SolidCube extends BaseCube {

    public SolidCube(float width, float height, float depth) {
        super(width, height, depth);
    }

    @Override
    public void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        final short indices[] = {
                0, 4, 5,
                0, 5, 1,
                1, 5, 6,
                1, 6, 2,
                2, 6, 7,
                2, 7, 3,
                3, 7, 4,
                3, 4, 0,
                4, 7, 6,
                4, 6, 5,
                3, 0, 1,
                3, 1, 2};

        setIndices(indices, IndicesOrder.TRIANGLES);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new SolidCube(width, height, depth);
    }
}

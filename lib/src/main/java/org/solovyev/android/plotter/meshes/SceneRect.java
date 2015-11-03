package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public class SceneRect extends BaseMesh implements DimensionsAware {
    @NonNull
    private Dimensions dimensions;

    public SceneRect(@NonNull Dimensions dimensions) {
        this.dimensions = dimensions;

    }

    @Override
    public void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        final float x = dimensions.scene.center.x;
        final float y = dimensions.scene.center.y;
        final float halfWidth = dimensions.scene.size.width / 2;
        final float halfHeight = dimensions.scene.size.height / 2;

        final float vertices[] = {
                -halfWidth + x, -halfHeight + y, 0, // 0
                halfWidth + x, -halfHeight + y, 0, // 1
                halfWidth + x, halfHeight + y, 0, // 2
                -halfWidth + x, halfHeight + y, 0, // 3
        };

        setVertices(vertices);

        final short indices[] = {
                0, 1,
                1, 2,
                2, 3,
                3, 0
        };

        setIndices(indices, IndicesOrder.LINES);
    }


    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new SceneRect(dimensions);
    }

    @NonNull
    @Override
    public Dimensions getDimensions() {
        return this.dimensions;
    }

    @Override
    public void setDimensions(@NonNull Dimensions dimensions) {
        if (!this.dimensions.equals(dimensions)) {
            this.dimensions = dimensions;
            setDirty();
        }
    }
}

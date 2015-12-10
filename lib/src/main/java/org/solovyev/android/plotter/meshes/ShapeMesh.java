package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL11;

public abstract class ShapeMesh extends BaseMesh implements DimensionsAware {

    @NonNull
    protected final MeshDimensions dimensions;
    @NonNull
    protected final PointF center;
    @NonNull
    protected final ShapePath path = new ShapePath();
    // create on the background thread and accessed from GL thread
    private volatile FloatBuffer verticesBuffer;
    private volatile ShortBuffer indicesBuffer;

    protected ShapeMesh(@NonNull Dimensions dimensions, @NonNull PointF center) {
        this.dimensions = new MeshDimensions(dimensions);
        this.center = center;
    }

    @Override
    protected void onInit() {
        super.onInit();
        final Dimensions dimensions = this.dimensions.get();

        path.clear();
        path.scale.set(dimensions.graph.scale);
        path.center.set(center);
        fillPath(path, dimensions);

        verticesBuffer = Meshes.allocateOrPutBuffer(path.vertices, path.start, path.length(), verticesBuffer);
        indicesBuffer = Meshes.allocateOrPutBuffer(path.getIndices(), 0, path.getIndicesCount(), indicesBuffer);
    }

    @Override
    public void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        Check.isNotNull(verticesBuffer);

        setVertices(verticesBuffer);
        setIndices(indicesBuffer, IndicesOrder.LINE_LOOP);
    }


    protected abstract void fillPath(@NonNull ShapePath path, @NonNull Dimensions dimensions);

    @NonNull
    @Override
    public Dimensions getDimensions() {
        return dimensions.get();
    }

    @Override
    public void setDimensions(@NonNull Dimensions dimensions) {
        if (this.dimensions.set(dimensions)) {
            setDirty();
        }
    }

    protected static class ShapePath extends Path {
        final PointF center = new PointF();
        final PointF scale = new PointF();

        public void setCenter(@NonNull PointF center) {
            offset(this.center.x - center.x, this.center.y - center.y);
        }

        private void offset(float x, float y) {
            for (int i = start; i < end; i += 3) {
                vertices[i] += x;
                vertices[i + 1] += y;
            }
        }

        protected void append(@NonNull Dimensions dimensions, float x, float y) {
            append(dimensions.graph.toScreenX(center.x + x), dimensions.graph.toScreenY(center.y + y));
        }
    }
}

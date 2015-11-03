package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public class Axis extends BaseMesh implements DimensionsAware {

    @NonNull
    private final AxisDirection direction;
    @NonNull
    private final Arrays arrays = new Arrays();
    @NonNull
    private final ArrayInitializer initializer = new ArrayInitializer();
    private final boolean d3;
    @NonNull
    protected volatile Dimensions dimensions;

    private Axis(@NonNull AxisDirection direction, @NonNull Dimensions dimensions, boolean d3) {
        this.direction = direction;
        this.dimensions = dimensions;
        this.d3 = d3;
    }

    @NonNull
    public static Axis x(@NonNull Dimensions dimensions, boolean d3) {
        return new Axis(AxisDirection.X, dimensions, d3);
    }

    @NonNull
    public static Axis y(@NonNull Dimensions dimensions, boolean d3) {
        return new Axis(AxisDirection.Y, dimensions, d3);
    }

    @NonNull
    public static Axis z(@NonNull Dimensions dimensions, boolean d3) {
        return new Axis(AxisDirection.Z, dimensions, d3);
    }

    @NonNull
    public DoubleBufferMesh<Axis> toDoubleBuffer() {
        return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
    }

    @Override
    protected void onInit() {
        super.onInit();

        if (!dimensions.scene.isEmpty()) {
            initializer.init();
            arrays.createBuffers();
        } else {
            setDirty();
        }
    }

    @Override
    protected void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        setVertices(arrays.getVerticesBuffer());
        setIndices(arrays.getIndicesBuffer(), IndicesOrder.LINES);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new Axis(direction, dimensions, d3);
    }

    @NonNull
    @Override
    public Dimensions getDimensions() {
        return this.dimensions;
    }

    @Override
    public void setDimensions(@NonNull Dimensions dimensions) {
        // todo serso: might be called on GL thread, requires synchronization
        if (!this.dimensions.equals(dimensions)) {
            this.dimensions = dimensions;
            setDirty();
        }
    }

    @Override
    public String toString() {
        return "Axis{" +
                "direction=" + direction +
                '}';
    }

    private class ArrayInitializer {

        private Scene.Axis axis;
        private Scene.Ticks ticks;

        public void init() {
            final Dimensions dimensions = Axis.this.dimensions;

            final boolean y = direction == AxisDirection.Y;
            axis = Scene.Axis.create(dimensions.scene, y, d3);
            ticks = Scene.Ticks.create(dimensions.graph, axis);
            arrays.init(3 * (2 + 2 + 2 * ticks.count), 2 + 2 * 2 + 2 * ticks.count);

            initLine(dimensions);
            initArrow();
            initTicks(dimensions);
        }

        private void initTicks(@NonNull Dimensions dimensions) {
            final int[] dv = direction.vector;
            final int[] da = direction.arrow;
            float x = -dv[0] * (ticks.axisLength / 2 + ticks.step + dimensions.scene.centerXForStep(ticks.step, d3)) + da[0] * ticks.width / 2 + (d3 ? dimensions.scene.center.x : 0);
            float y = -dv[1] * (ticks.axisLength / 2 + ticks.step + dimensions.scene.centerYForStep(ticks.step, d3)) + da[1] * ticks.width / 2 + (d3 ? dimensions.scene.center.y : 0);
            float z = -dv[2] * (ticks.axisLength / 2 + ticks.step) + da[2] * ticks.width / 2;
            for (int i = 0; i < ticks.count; i++) {
                x += dv[0] * ticks.step;
                y += dv[1] * ticks.step;
                z += dv[2] * ticks.step;

                arrays.add(arrays.vertex / 3, x, y, z);
                arrays.add(arrays.vertex / 3, x - da[0] * ticks.width, y - da[1] * ticks.width, z - da[2] * ticks.width);
            }
        }

        private void initArrow() {
            final int[] dv = direction.vector;
            final int[] da = direction.arrow;
            arrays.add(0,
                    arrays.vertices[0] - dv[0] * axis.arrowLength - da[0] * axis.arrowWidth / 2,
                    arrays.vertices[1] - dv[1] * axis.arrowLength - da[1] * axis.arrowWidth / 2,
                    arrays.vertices[2] - dv[2] * axis.arrowLength - da[2] * axis.arrowWidth / 2);
            arrays.indices[arrays.index++] = 2;

            arrays.add(0,
                    arrays.vertices[0] - dv[0] * axis.arrowLength + da[0] * axis.arrowWidth / 2,
                    arrays.vertices[1] - dv[1] * axis.arrowLength + da[1] * axis.arrowWidth / 2,
                    arrays.vertices[2] - dv[2] * axis.arrowLength + da[2] * axis.arrowWidth / 2);
            arrays.indices[arrays.index++] = 3;
        }

        private void initLine(@NonNull Dimensions dimensions) {
            final int[] dv = direction.vector;
            final float x = axis.length / 2 + (!d3 ? dimensions.scene.center.x : 0);
            final float y = axis.length / 2 + (!d3 ? dimensions.scene.center.y : 0);
            final float z = axis.length / 2;

            arrays.add(0, dv[0] * x + (d3 ? dimensions.scene.center.x : 0), dv[1] * y + (d3 ? dimensions.scene.center.y : 0), dv[2] * z);
            arrays.add(1,
                    arrays.vertices[0] - dv[0] * axis.length,
                    arrays.vertices[1] - dv[1] * axis.length,
                    arrays.vertices[2] - dv[2] * axis.length);
        }
    }
}

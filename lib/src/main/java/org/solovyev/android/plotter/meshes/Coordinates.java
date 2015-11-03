package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public class Coordinates extends BaseMesh implements DimensionsAware {
    private static final float EMPTY = Float.MAX_VALUE;
    private final short indices[] = {
            0, 1,
            2, 3};
    private volatile float x = EMPTY;
    private volatile float y = EMPTY;
    private final float vertices[] = {
            x, 0, 0,
            x, 0, 0,
            0, y, 0,
            0, y, 0};
    @NonNull
    private volatile Dimensions dimensions;
    @Nullable
    private volatile Data data;

    public Coordinates(@NonNull Dimensions dimensions, @NonNull Color color) {
        this.dimensions = dimensions;
        setColor(color);
    }

    @Override
    protected void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        if (isEmpty() || dimensions.isZero()) {
            setDirtyGl();
            return;
        }

        final Data data = getData();

        if (x < -data.xTicks.axisLength / 2 || x > data.xTicks.axisLength / 2 || y < -data.yTicks.axisLength / 2 || y > data.yTicks.axisLength / 2) {
            setDirtyGl();
            return;
        }

        setIndices(indices, IndicesOrder.LINES);

        int vertex = 0;
        vertices[vertex] = x;
        vertices[vertex + 1] = -data.yTicks.axisLength / 2;
        vertex += 3;
        vertices[vertex] = x;
        vertices[vertex + 1] = data.yTicks.axisLength / 2;
        vertex += 3;
        vertices[vertex] = -data.xTicks.axisLength / 2;
        vertices[vertex + 1] = y;
        vertex += 3;
        vertices[vertex] = data.xTicks.axisLength / 2;
        vertices[vertex + 1] = y;
        setVertices(vertices);
    }

    @NonNull
    private Data getData() {
        Data localData = data;
        if (localData == null) {
            localData = new Data(dimensions);
            data = localData;
        }
        return localData;
    }

    private boolean isEmpty() {
        return x == EMPTY || y == EMPTY;
    }

    public void set(float x, float y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            setDirtyGl();
        }
    }

    public void setScreenXY(float x, float y) {
        set(dimensions.scene.toSceneX(x), dimensions.scene.toSceneY(y));
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return this;
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
            this.data = null;
        }
    }

    public void clear() {
        set(EMPTY, EMPTY);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "y=" + y +
                ", x=" + x +
                '}';
    }

    private static final class Data {
        @NonNull
        final Scene.Axis xAxis;
        @NonNull
        final Scene.Axis yAxis;
        @NonNull
        final Scene.Ticks xTicks;
        @NonNull
        final Scene.Ticks yTicks;

        private Data(@NonNull Dimensions dimensions) {
            xAxis = Scene.Axis.create(dimensions.scene, false, false);
            yAxis = Scene.Axis.create(dimensions.scene, true, false);
            xTicks = Scene.Ticks.create(dimensions.graph, xAxis);
            yTicks = Scene.Ticks.create(dimensions.graph, yAxis);
        }
    }
}

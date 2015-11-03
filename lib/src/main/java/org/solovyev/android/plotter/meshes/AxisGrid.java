package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.Dimensions;

public class AxisGrid extends BaseSurface {

    private final boolean d3;
    @NonNull
    private Axes axes;

    private AxisGrid(@NonNull Dimensions dimensions, @NonNull Axes axes, @NonNull Color color, boolean d3) {
        super(dimensions);
        this.axes = axes;
        this.d3 = d3;
        setColor(color);
    }

    @NonNull
    public static AxisGrid yz(@NonNull Dimensions dimensions, @NonNull Color color, boolean d3) {
        return new AxisGrid(dimensions, Axes.YZ, color, d3);
    }

    @NonNull
    public static AxisGrid xz(@NonNull Dimensions dimensions, @NonNull Color color, boolean d3) {
        return new AxisGrid(dimensions, Axes.XZ, color, d3);
    }

    @NonNull
    public static AxisGrid xy(@NonNull Dimensions dimensions, @NonNull Color color, boolean d3) {
        return new AxisGrid(dimensions, Axes.XY, color, d3);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new AxisGrid(dimensions, axes, getColor(), d3);
    }

    @NonNull
    public DoubleBufferMesh<AxisGrid> toDoubleBuffer() {
        return DoubleBufferMesh.wrap(this, DimensionsAwareSwapper.INSTANCE);
    }

    @NonNull
    @Override
    protected SurfaceInitializer createInitializer() {
        final Scene.AxisGrid grid = Scene.AxisGrid.create(dimensions, axes, d3);
        return new SurfaceInitializer(this, SurfaceInitializer.Data.create(grid.rect, grid.widthTicks.count, grid.heightTicks.count)) {
            @Override
            protected void rotate(float[] point) {
                if (axes != Axes.XY) {
                    final float x = point[0];
                    final float y = point[1];
                    final float z = point[2];
                    switch (axes) {
                        case XZ:
                            point[0] = x;
                            point[1] = z;
                            point[2] = y;
                            break;
                        case YZ:
                            point[0] = z;
                            point[1] = y;
                            point[2] = x;
                            break;
                    }
                }
                point[0] += (d3 ? dimensions.scene.center.x : 0);
                point[2] += (d3 ? dimensions.scene.center.y : 0);
            }
        };
    }

    @Override
    protected float z(float x, float y, int xi, int yi) {
        return 0;
    }

    @Override
    public String toString() {
        return "AxisGrid{" +
                "axes=" + axes +
                '}';
    }

    protected static enum Axes {
        XY,
        XZ,
        YZ;
    }
}
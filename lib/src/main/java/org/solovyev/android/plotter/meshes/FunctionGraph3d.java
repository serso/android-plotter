package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

public class FunctionGraph3d extends BaseSurface implements FunctionGraph {

    @NonNull
    private volatile Function function;
    private volatile int pointsCount = MeshSpec.DEFAULT_POINTS_COUNT;

    FunctionGraph3d(@NonNull Dimensions dimensions, @NonNull Function function, int pointsCount) {
        super(dimensions);
        this.function = function;
        this.pointsCount = pointsCount;
    }

    @NonNull
    public static FunctionGraph3d create(@NonNull Dimensions dimensions, @NonNull Function function, int pointsCount) {
        return new FunctionGraph3d(dimensions, function, pointsCount);
    }

    @NonNull
    @Override
    protected SurfaceInitializer createInitializer() {
        final int size = pointsCount == MeshSpec.DEFAULT_POINTS_COUNT ? 20 : pointsCount * Scene.getMultiplier(true);
        return new SurfaceInitializer.GraphSurfaceInitializer(this, dimensions.graph, size);
    }

    @Override
    protected float z(float x, float y, int xi, int yi) {
        final Function f = function;
        switch (f.getArity()) {
            case 0:
                return f.evaluate();
            case 1:
                return f.evaluate(x);
            case 2:
                return f.evaluate(x, y);
            default:
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    @Override
    public Function getFunction() {
        return function;
    }

    @Override
    public void setFunction(@NonNull Function function) {
        // todo serso: might be called on GL thread, requires synchronization
        if (!this.function.equals(function)) {
            this.function = function;
            setDirty();
        }
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new FunctionGraph3d(dimensions, function, pointsCount);
    }

    @Override
    public String toString() {
        return function.toString();
    }

    @Override
    public int getPointsCount() {
        return pointsCount;
    }

    @Override
    public void setPointsCount(int pointsCount) {
        if (this.pointsCount == pointsCount) {
            return;
        }
        this.pointsCount = pointsCount;
        setDirty();
    }
}

package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

public class FunctionGraph3d extends BaseSurface implements FunctionGraph {

    @NonNull
    private volatile Function function;

    FunctionGraph3d(@NonNull Dimensions dimensions, @NonNull Function function) {
        super(dimensions);
        this.function = function;
    }

    @NonNull
    public static FunctionGraph3d create(@NonNull Dimensions dimensions, @NonNull Function function) {
        return new FunctionGraph3d(dimensions, function);
    }

    @NonNull
    @Override
    protected SurfaceInitializer createInitializer() {
        return new SurfaceInitializer.GraphSurfaceInitializer(this, dimensions.graph);
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
        return new FunctionGraph3d(dimensions, function);
    }

    @Override
    public String toString() {
        return function.toString();
    }
}

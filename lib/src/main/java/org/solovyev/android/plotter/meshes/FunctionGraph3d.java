package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

import javax.annotation.Nonnull;

public class FunctionGraph3d extends BaseSurface implements FunctionGraph {

    @Nonnull
    private volatile Function function;

    FunctionGraph3d(@Nonnull Dimensions dimensions, @Nonnull Function function) {
        super(dimensions);
        this.function = function;
    }

    @Nonnull
    public static FunctionGraph3d create(@Nonnull Dimensions dimensions, @Nonnull Function function) {
        return new FunctionGraph3d(dimensions, function);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Function getFunction() {
        return function;
    }

    @Override
    public void setFunction(@Nonnull Function function) {
        // todo serso: might be called on GL thread, requires synchronization
        if (!this.function.equals(function)) {
            this.function = function;
            setDirty();
        }
    }

    @Nonnull
    @Override
    protected BaseMesh makeCopy() {
        return new FunctionGraph3d(dimensions, function);
    }

    @Override
    public String toString() {
        return function.toString();
    }
}

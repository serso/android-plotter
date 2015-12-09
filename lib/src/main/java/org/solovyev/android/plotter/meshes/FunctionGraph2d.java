package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.Function;

public class FunctionGraph2d extends BaseCurve implements FunctionGraph {

    @NonNull
    private volatile Function function;
    private volatile int pointsCount;

    private FunctionGraph2d(@NonNull Dimensions dimensions, @NonNull Function function, int pointsCount) {
        super(dimensions);
        this.function = function;
        this.pointsCount = pointsCount;
    }

    @NonNull
    public static FunctionGraph2d create(@NonNull Dimensions dimensions, @NonNull Function function, int pointsCount) {
        return new FunctionGraph2d(dimensions, function, pointsCount);
    }

    @Override
    protected float y(float x) {
        final Function f = function;
        switch (f.getArity()) {
            case 0:
                return f.evaluate();
            case 1:
                return f.evaluate(x);
            case 2:
                return f.evaluate(x, 0);
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
        return create(dimensions.get(), function, getPointsCount());
    }

    @Override
    public String toString() {
        return function.toString() + "(" + Integer.toString(System.identityHashCode(this), 16) + ")";
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

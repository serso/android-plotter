package org.solovyev.android.plotter;

import android.support.annotation.Nullable;

public abstract class Function1 extends Function {

    protected Function1() {
    }

    protected Function1(@Nullable String name) {
        super(name);
    }

    @Override
    public final int getArity() {
        return 1;
    }

    @Override
    public final float evaluate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final float evaluate(float x, float y) {
        throw new UnsupportedOperationException();
    }
}

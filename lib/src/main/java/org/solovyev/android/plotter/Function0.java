package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public abstract class Function0 extends Function {

    @Nonnull
    public static final Function0 ZERO = new Function0("0") {
        @Override
        public float evaluate() {
            return 0;
        }
    };

    protected Function0() {
    }

    protected Function0(@Nonnull String name) {
        super(name);
    }

    @Override
    public final int getArity() {
        return 0;
    }

    @Override
    public final float evaluate(float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final float evaluate(float x, float y) {
        throw new UnsupportedOperationException();
    }
}

package org.solovyev.android.plotter;

public abstract class Function1 extends Function {
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

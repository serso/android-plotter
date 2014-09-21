package org.solovyev.android.plotter;

public abstract class Function0 extends Function {
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

package org.solovyev.android.plotter;

public abstract class Function2 extends Function {
	@Override
	public final int getArity() {
		return 2;
	}

	@Override
	public final float evaluate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final float evaluate(float x) {
		throw new UnsupportedOperationException();
	}
}

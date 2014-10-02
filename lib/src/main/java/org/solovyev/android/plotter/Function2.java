package org.solovyev.android.plotter;

import javax.annotation.Nullable;

public abstract class Function2 extends Function {

	protected Function2() {
	}

	protected Function2(@Nullable String name) {
		super(name);
	}

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

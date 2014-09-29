package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class Plot {

	private Plot() {
		throw new AssertionError();
	}

	@Nonnull
	public static Plotter newPlotter() {
		return new DefaultPlotter();
	}
}

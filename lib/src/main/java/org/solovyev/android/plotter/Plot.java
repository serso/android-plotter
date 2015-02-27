package org.solovyev.android.plotter;

import android.content.Context;
import android.os.Looper;

import javax.annotation.Nonnull;

public final class Plot {

	private Plot() {
		throw new AssertionError();
	}

	@Nonnull
	public static String getTag() {
		return "Plot";
	}

	@Nonnull
	public static String getTag(@Nonnull String tag) {
		return getTag() + "/" + tag;
	}

	@Nonnull
	public static Plotter newPlotter(@Nonnull Context context) {
		return new DefaultPlotter(context);
	}

	static boolean isMainThread() {
		return Looper.getMainLooper() == Looper.myLooper();
	}
}

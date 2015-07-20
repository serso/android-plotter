package org.solovyev.android.plotter;

import android.content.Context;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;

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

	public static float dpsToPxs(@Nonnull Context context, float dps) {
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, dm);
	}

	public static long animationTime() {
		return System.nanoTime() / (1000L * 1000L);
	}
}

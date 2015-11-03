package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class Plot {

    @NonNull
    public static final PointF ZERO = new PointF();

    private Plot() {
        throw new AssertionError();
    }

    @NonNull
    public static String getTag() {
        return "Plot";
    }

    @NonNull
    public static String getTag(@NonNull String tag) {
        return getTag() + "/" + tag;
    }

    @NonNull
    public static Plotter newPlotter(@NonNull Context context) {
        return new DefaultPlotter(context);
    }

    static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static float dpsToPxs(@NonNull Context context, float dps) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, dm);
    }

    public static long animationTime() {
        return System.nanoTime() / (1000L * 1000L);
    }

    static int getAvailableProcessors() {
        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }
}

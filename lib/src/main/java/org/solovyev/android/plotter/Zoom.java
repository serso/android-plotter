package org.solovyev.android.plotter;

import android.os.Bundle;
import android.support.annotation.NonNull;

public final class Zoom {

    public static final float ZOOM_LEVEL_MIN = (float) Math.pow(0.1, 4);
    public static final float ZOOM_LEVEL_MAX = (float) Float.MAX_VALUE;
    private static final float ZOOM_SPEED = 1f;
    private static final float ZOOM_IN = .625f / ZOOM_SPEED;
    private static final float ZOOM_OUT = 1.6f * ZOOM_SPEED;
    private static final Zoom ONE = new Zoom(1f, 1f, 1f);

    final float level;
    final float x;
    final float y;

    public Zoom(float level, float x, float y) {
        this.level = level;
        this.x = x;
        this.y = y;
    }

    @NonNull
    public static Zoom one() {
        return ONE;
    }

    @NonNull
    public static Zoom between(@NonNull Zoom from, @NonNull Zoom to, float interpolation) {
        final float level = between(from.level, to.level, interpolation);
        final float x = between(from.x, to.x, interpolation);
        final float y = between(from.y, to.y, interpolation);
        return new Zoom(level, x, y);
    }

    private static float between(float from, float to, float interpolation) {
        if (from == to) {
            return from;
        } else if (from > to) {
            return from - (from - to) * interpolation;
        } else {
            return from + (to - from) * interpolation;
        }
    }

    @NonNull
    public static Zoom load(@NonNull Bundle bundle) {
        final float level = bundle.getFloat("zoom.level", 1f);
        final float x = bundle.getFloat("zoom.x", 1f);
        final float y = bundle.getFloat("zoom.y", 1f);
        return new Zoom(level, x, y);
    }

    public void save(@NonNull Bundle bundle) {
        bundle.putFloat("zoom.level", level);
        bundle.putFloat("zoom.x", x);
        bundle.putFloat("zoom.y", y);
    }

    @NonNull
    public Zoom multiplyBy(float level) {
        return new Zoom(level * this.level, x, y);
    }

    @NonNull
    public Zoom multiplyBy(float x, float y) {
        return new Zoom(level, x * this.x, y * this.y);
    }

    static boolean canZoom(float zoomChange, float graphSize) {
        if (zoomChange == 1f) {
            return true;
        }
        if (zoomChange > 1f) {
            return graphSize <= ZOOM_LEVEL_MAX / zoomChange;
        }
        return graphSize * zoomChange >= ZOOM_LEVEL_MIN;
    }

    static boolean canZoomIn(@NonNull Dimensions dimensions) {
        return canZoom(ZOOM_IN, dimensions.graph.size.min());
    }

    static boolean canZoomOut(@NonNull Dimensions dimensions) {
        return canZoom(ZOOM_OUT, dimensions.graph.size.min());
    }

    boolean isOne() {
        return level == 1f && x == 1f && y == 1f;
    }

    @NonNull
    public Zoom zoomIn() {
        return multiplyBy(ZOOM_IN);
    }

    @NonNull
    public Zoom zoomOut() {
        return multiplyBy(ZOOM_OUT);
    }

    public boolean smallerThan(@NonNull Zoom that) {
        return level < that.level;
    }

    public boolean biggerThan(@NonNull Zoom that) {
        return that.smallerThan(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zoom zoom = (Zoom) o;

        if (Float.compare(zoom.level, level) != 0) return false;
        if (Float.compare(zoom.x, x) != 0) return false;
        if (Float.compare(zoom.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (level != +0.0f ? Float.floatToIntBits(level) : 0);
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Zoom{" +
                "level=" + level +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

package org.solovyev.android.plotter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import static android.os.SystemClock.uptimeMillis;

final class Zoomer {

    static final long DURATION = 1000L;
    @NonNull
    private final Interpolator interpolator = new AccelerateDecelerateInterpolator();
    private long duration = DURATION;
    @NonNull
    private Zoom current = Zoom.one();
    @NonNull
    private Zoom to = current;
    @Nullable
    private Zoom from = null;
    private long startTime = -1;

    public Zoomer(@NonNull Bundle bundle) {
        current = Zoom.load(bundle);
        to = current;
    }

    public Zoomer() {
    }

    @NonNull
    public Zoom current() {
        return current;
    }

    /**
     * @return true if current zoom level has changed
     */
    public boolean onFrame() {
        if (isZooming()) {
            Check.isTrue(from != null);
            final long now = uptimeMillis();
            final float position = (now - startTime) / (float) duration;
            if (position >= 1f) {
                startTime = -1;
                from = null;
                current = to;
                return true;
            }

            if (!from.equals(to)) {
                current = Zoom.between(from, to, interpolator.getInterpolation(position));
                return true;
            }
        }

        return false;
    }

    public boolean zoom(boolean in) {
        if ((isZoomingIn() && in) || (isZoomingOut() && !in)) {
            return false;
        }

        if (in) {
            if (!current.canZoomIn()) {
                return false;
            }
            if (from == null) {
                zoomTo(current.zoomIn());
            } else {
                reverseZoom();
            }
        } else {
            if (!current.canZoomOut()) {
                return false;
            }
            if (from == null) {
                zoomTo(current.zoomOut());
            } else {
                reverseZoom();
            }
        }

        return true;
    }

    public boolean zoomBy(float level) {
        if (isZooming()) {
            return false;
        }
        if (level == 1f) {
            return false;
        }
        zoomTo(current.multiplyBy(level));
        duration = 1;
        return true;
    }

    public boolean zoomBy(float x, float y) {
        if (isZooming()) {
            return false;
        }
        if (x == 1f && y == 1f) {
            return false;
        }
        zoomTo(current.multiplyBy(x, y));
        duration = 1;
        return true;
    }

    private void zoomTo(@NonNull Zoom newZoom) {
        to = newZoom;
        from = current;
        duration = DURATION;
        startTime = uptimeMillis();
    }

    public boolean reset() {
        if (isZooming()) {
            if (to.isOne() || (from != null && from.isOne())) {
                reverseZoom();
                return true;
            }
            return false;
        }

        if (!current.isOne()) {
            zoomTo(Zoom.one());
            return true;
        }

        return false;
    }

    private void reverseZoom() {
        final Zoom tmp = to;
        to = from;
        from = tmp;

        final long now = uptimeMillis();
        final long remaining = duration - (now - startTime);
        startTime = now - remaining;
    }

    boolean isZooming() {
        return to != current;
    }

    boolean isZoomingIn() {
        return to.smallerThan(current);
    }

    boolean isZoomingOut() {
        return to.biggerThan(current);
    }

    public void saveState(@NonNull Bundle bundle) {
        final Zoom zoom = isZooming() ? to : current;
        zoom.save(bundle);
    }

    @Override
    public String toString() {
        return "Zoomer{" +
                "current=" + (isZooming() ? to : current) +
                '}';
    }
}

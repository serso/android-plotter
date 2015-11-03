package org.solovyev.android.plotter;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class CameraMan {
    private final long duration = Zoomer.DURATION;
    @NonNull
    private final Interpolator interpolator = new DecelerateInterpolator();

    @NonNull
    private final PointF current = new PointF();
    @NonNull
    private final PointF from = new PointF();
    @NonNull
    private final PointF to = new PointF();
    private long startTime;

    void move(@NonNull PointF from, @NonNull PointF to) {
        startTime = Plot.animationTime();
        this.from.set(from);
        this.to.set(to);
        this.current.set(from);
    }

    private boolean isAnimating() {
        return startTime != 0 && Plot.animationTime() - startTime < duration;
    }

    boolean onFrame() {
        if (!isAnimating()) {
            return false;
        }
        final long elapsed = Math.min(Plot.animationTime() - startTime, duration);
        final float position = elapsed / (float) duration;
        final float interpolation = interpolator.getInterpolation(position);
        current.x = from.x + interpolation * (to.x - from.x);
        current.y = from.y + interpolation * (to.y - from.y);

        return isAnimating();
    }

    @NonNull
    public PointF getPosition() {
        if (isAnimating()) {
            return current;
        } else {
            return to;
        }
    }
}

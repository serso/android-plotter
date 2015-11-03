package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class Fader {

    private static final long DURATION = 250;
    @NonNull
    private final Interpolator interpolator = new DecelerateInterpolator();
    @NonNull
    private FadingState state = FadingState.NONE;

    private float alpha = 1f;
    private long startTime;

    void fadeIn() {
        if (state == FadingState.IN || state == FadingState.NONE) {
            return;
        }
        state = FadingState.IN;
        startOrReverse();
    }

    private void startOrReverse() {
        if (!isAnimating()) {
            startTime = Plot.animationTime();
        } else {
            reverse();
        }
    }

    private boolean isAnimating() {
        return startTime != 0 && state != FadingState.NONE && Plot.animationTime() - startTime < DURATION;
    }

    void fadeOut() {
        if (state == FadingState.OUT) {
            return;
        }
        state = FadingState.OUT;
        startOrReverse();
    }

    boolean onFrame() {
        if (!isAnimating()) {
            return false;
        }
        final long elapsed = Math.min(Plot.animationTime() - startTime, DURATION);
        final float position = elapsed / (float) DURATION;
        switch (state) {
            case IN:
                alpha = interpolator.getInterpolation(position);
                break;
            case OUT:
                alpha = 1f - interpolator.getInterpolation(position);
                break;
            case NONE:
                Check.isTrue(alpha == 1f);
                break;
        }

        return isAnimating();
    }

    private void reverse() {
        final long now = Plot.animationTime();
        final long remaining = DURATION - (now - startTime);
        startTime = now - remaining;
    }

    public float getAlpha() {
        if (isAnimating()) {
            return alpha;
        } else if (state == FadingState.OUT) {
            return 0;
        } else {
            return 1;
        }
    }

    private enum FadingState {
        IN,
        OUT,
        NONE;
    }
}

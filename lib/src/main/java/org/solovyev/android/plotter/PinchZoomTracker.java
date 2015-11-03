package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;

final class PinchZoomTracker {

    @NonNull
    private static final String TAG = Plot.getTag("PinchZoom");

    private static final float EPS = 1.5f;

    @NonNull
    private final PointF distance = new PointF();

    @NonNull
    private final ZoomLevels current = new ZoomLevels();

    private final float minDistance;

    PinchZoomTracker(@NonNull Context context) {
        minDistance = Plot.dpsToPxs(context, 15);
    }

    private static float distance(float from, float to) {
        final float dx = from - to;
        return Math.abs(dx);
    }

    void reset(float x1, float y1,
               float x2, float y2) {
        distance.x = distance(x1, x2);
        distance.y = distance(y1, y2);
    }

    @NonNull
    ZoomLevels update(float x1, float y1, float x2, float y2) {
        current.reset();

        if (distance.x > minDistance) {
            final float dx = distance(x1, x2);
            if (dx > EPS) {
                current.setX(getZoom(distance.x, dx));
            }
        }

        if (distance.y > minDistance) {
            final float dy = distance(y1, y2);
            if (dy > EPS) {
                current.setY(getZoom(distance.y, dy));
            }
        }

        if (current.isChanged()) {
            Log.d(TAG, String.valueOf(current));
        }

        return current;
    }

    private float getZoom(float z1, float z2) {
        return (float) Math.pow(z1 / z2, 1f / 3);
    }
}

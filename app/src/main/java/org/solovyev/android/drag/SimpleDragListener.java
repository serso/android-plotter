package org.solovyev.android.drag;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import org.solovyev.android.plotter.app.R;

import java.util.EnumMap;

public class SimpleDragListener implements DragListener {

    @NonNull
    private static final PointF axis = new PointF(0, 1);

    @NonNull
    private static final EnumMap<DragDirection, Interval<Float>> sAngleIntervals = new EnumMap<>(DragDirection.class);

    static {
        for (DragDirection direction : DragDirection.values()) {
            sAngleIntervals.put(direction, makeAngleInterval(direction, 0, 45));
        }
    }

    @NonNull
    private final DragProcessor processor;

    private final float minDistancePxs;

    public SimpleDragListener(@NonNull DragProcessor processor, @NonNull Context context) {
        this.processor = processor;
        this.minDistancePxs = context.getResources().getDimensionPixelSize(R.dimen.drag_min_distance);
    }

    @NonNull
    private static Interval<Float> makeAngleInterval(@NonNull DragDirection direction,
                                                     float leftLimit,
                                                     float rightLimit) {
        final Float newLeftLimit;
        final Float newRightLimit;
        switch (direction) {
            case up:
                newLeftLimit = 180f - rightLimit;
                newRightLimit = 180f - leftLimit;
                break;
            case down:
                newLeftLimit = leftLimit;
                newRightLimit = rightLimit;
                break;
            case left:
                newLeftLimit = 90f - rightLimit;
                newRightLimit = 90f + rightLimit;
                break;
            case right:
                newLeftLimit = 90f - rightLimit;
                newRightLimit = 90f + rightLimit;
                break;
            default:
                throw new AssertionError();
        }

        return Intervals.newClosedInterval(newLeftLimit, newRightLimit);
    }

    @Override
    public boolean onDrag(@NonNull DragButton dragButton, @NonNull DragEvent event) {
        boolean consumed = false;

        final MotionEvent motionEvent = event.getMotionEvent();

        final PointF start = event.getStartPoint();
        final PointF end = new PointF(motionEvent.getX(), motionEvent.getY());
        final float distance = Maths.getDistance(start, end);

        final MutableObject<Boolean> right = new MutableObject<>();
        final double angle = Math.toDegrees(Maths.getAngle(start, Maths.sum(start, axis), end, right));

        final long duration = motionEvent.getEventTime() - motionEvent.getDownTime();
        final DragDirection direction = getDirection(distance, (float) angle, right.getObject());
        if (direction != null && duration > 40 && duration < 2500) {
            consumed = processor.processDragEvent(direction, dragButton, start, motionEvent);
        }

        return consumed;
    }

    @Nullable
    private DragDirection getDirection(float distance, float angle, boolean right) {
        if (distance > minDistancePxs) {
            for (DragDirection direction : DragDirection.values()) {
                final Interval<Float> angleInterval = sAngleIntervals.get(direction);
                final boolean wrongDirection = (direction == DragDirection.left && right) ||
                        (direction == DragDirection.right && !right);
                if (!wrongDirection && angleInterval.contains(angle)) {
                    return direction;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSuppressOnClickEvent() {
        return true;
    }

    public interface DragProcessor {

        boolean processDragEvent(@NonNull DragDirection dragDirection, @NonNull DragButton dragButton, @NonNull PointF startPoint2d, @NonNull MotionEvent motionEvent);
    }
}
package org.solovyev.android.drag;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

public class DragEvent {

    @NonNull
    private final PointF startPoint;

    @NonNull
    private final MotionEvent motionEvent;

    public DragEvent(@NonNull PointF startPoint, @NonNull MotionEvent motionEvent) {
        this.startPoint = startPoint;
        this.motionEvent = motionEvent;
    }

    /**
     * @return motion event started at start point
     */
    @NonNull
    public MotionEvent getMotionEvent() {
        return motionEvent;
    }

    /**
     * @return start point of dragging
     */
    @NonNull
    public PointF getStartPoint() {
        return startPoint;
    }


}

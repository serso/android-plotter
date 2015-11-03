/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.plotter;

import android.os.Build;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import javax.annotation.Nonnull;

final class TouchHandler implements View.OnTouchListener {

    @Nonnull
    private final VelocityTracker tracker = VelocityTracker.obtain();
    @Nonnull
    private final Listener listener;
    private boolean afterZoom;

    private TouchHandler(@Nonnull Listener listener) {
        this.listener = listener;
    }

    static TouchHandler create(@Nonnull Listener listener) {
        return new TouchHandler(listener);
    }

    private static float getX(@Nonnull MotionEvent event, int pointer) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ? event.getX(pointer) : 0;
    }

    private static float getY(@Nonnull MotionEvent event, int pointer) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ? event.getY(pointer) : 0;
    }

    private static int getPointerCount(@Nonnull MotionEvent event) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR ? event.getPointerCount() : 1;
    }

    @Override
    public boolean onTouch(@Nonnull View v, @Nonnull MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        listener.onTouch(x, y);

        final int pointerCount = getPointerCount(event);
        switch (getAction(event)) {
            case MotionEvent.ACTION_DOWN:
                afterZoom = false;
                tracker.clear();
                tracker.addMovement(event);
                listener.onTouchDown(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1) {
                    if (afterZoom) {
                        tracker.clear();
                        listener.onTouchDown(x, y);
                        afterZoom = false;
                    }
                    tracker.addMovement(event);
                    listener.onTouchMove(x, y);
                } else if (pointerCount == 2) {
                    listener.onTouchZoomMove(x, y, getX(event, 1), getY(event, 1));
                }
                break;

            case MotionEvent.ACTION_UP:
                tracker.addMovement(event);
                tracker.computeCurrentVelocity(1000);
                listener.onTouchUp(x, y);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    listener.onTouchZoomDown(x, y, getX(event, 1), getY(event, 1));
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (pointerCount == 2) {
                    listener.onTouchZoomUp(x, y, getX(event, 1), getY(event, 1));
                    afterZoom = true;
                }
                break;
        }
        return true;
    }

    private int getAction(MotionEvent event) {
        return event.getAction() & MotionEvent.ACTION_MASK;
    }

    public float getXVelocity() {
        return tracker.getXVelocity();
    }

    public float getYVelocity() {
        return tracker.getYVelocity();
    }

    interface Listener {
        void onTouch(float x, float y);

        void onTouchDown(float x, float y);

        void onTouchMove(float x, float y);

        void onTouchUp(float x, float y);

        void onTouchZoomDown(float x1, float y1, float x2, float y2);

        void onTouchZoomMove(float x1, float y1, float x2, float y2);

        void onTouchZoomUp(float x1, float y1, float x2, float y2);
    }
}

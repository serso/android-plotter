package org.solovyev.android.views;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public final class LongClickEraser implements View.OnTouchListener, View.OnClickListener {

    @NonNull
    private final View view;

    @NonNull
    private final EditText editText;

    @NonNull
    private final GestureDetector gestureDetector;

    @NonNull
    private final Eraser eraser = new Eraser();

    private LongClickEraser(@NonNull final View view, @NonNull EditText editText) {
        this.view = view;
        this.editText = editText;
        this.gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                if (eraser.isTracking()) {
                    eraser.start();
                }
            }
        });
    }

    public static void createAndAttach(@NonNull View view, @NonNull EditText editText) {
        final LongClickEraser l = new LongClickEraser(view, editText);
        view.setOnClickListener(l);
        view.setOnTouchListener(l);
    }

    private static void erase(@NonNull EditText editText) {
        final int start = clampSelection(editText.getSelectionStart());
        final int end = clampSelection(editText.getSelectionEnd());
        if (start != end) {
            editText.getText().delete(Math.min(start, end), Math.max(start, end));
        } else if (start > 0) {
            editText.getText().delete(start - 1, start);
        }
    }

    public static int clampSelection(int selection) {
        return selection < 0 ? 0 : selection;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                eraser.stopTracking();
                break;
            default:
                eraser.startTracking();
                gestureDetector.onTouchEvent(event);
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        erase(editText);
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private class Eraser implements Runnable {
        private static final int DELAY = 300;
        private long delay;
        private boolean erasing;
        private boolean tracking = true;

        @Override
        public void run() {
            erase(editText);
            if (editText.length() == 0 || clampSelection(editText.getSelectionStart()) == 0) {
                stop();
                return;
            }
            delay = Math.max(50, 2 * delay / 3);
            view.postDelayed(this, delay);
        }

        void start() {
            if (erasing) {
                stop();
            }
            erasing = true;
            delay = DELAY;
            view.removeCallbacks(this);
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            run();
        }

        void stop() {
            view.removeCallbacks(this);
            if (!erasing) {
                return;
            }

            erasing = false;
        }

        public void stopTracking() {
            stop();
            tracking = false;
        }

        public boolean isTracking() {
            return tracking;
        }

        public void startTracking() {
            tracking = true;
        }
    }
}

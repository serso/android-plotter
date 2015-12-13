package org.solovyev.android.plotter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.uptimeMillis;

public class PlotView extends GLSurfaceView implements PlottingView {

    @NonNull
    private final PlotRenderer renderer;
    @NonNull
    private final TouchListener touchListener = new TouchListener();
    @NonNull
    private final List<Listener> listeners = new ArrayList<>();
    @Nullable
    private Plotter plotter;
    private boolean attached;
    private boolean d3 = Plotter.D3;
    private boolean d3Initialized;

    public PlotView(Context context) {
        super(context);
        init();
        renderer = initGl(this);
    }

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        renderer = initGl(this);
    }

    @NonNull
    private static PlotRenderer initGl(@NonNull PlotView view) {
        view.setEGLConfigChooser(new MultisampleConfigChooser());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preserveEglContextOnPause(view);
        }

        final PlotRenderer renderer = new PlotRenderer(view);
        view.setRenderer(renderer);

        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        return renderer;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void preserveEglContextOnPause(@NonNull PlotView view) {
        view.setPreserveEGLContextOnPause(true);
    }

    private void init() {
        setOnTouchListener(touchListener.handler);
        touchListener.on3d(d3);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (plotter != null) {
            attachToPlotter(plotter);
        }
        attached = true;
    }

    private void attachToPlotter(@NonNull Plotter plotter) {
        plotter.attachView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        if (plotter != null) {
            detachFromPlotter(plotter);
        }
        d3Initialized = false;
        super.onDetachedFromWindow();
    }

    private void detachFromPlotter(@NonNull Plotter plotter) {
        plotter.detachView(this);
    }

    public void setPlotter(@NonNull Plotter plotter) {
        Check.isMainThread();
        Check.isNull(this.plotter);
        this.plotter = plotter;
        this.renderer.setPlotter(plotter);
        if (attached) {
            attachToPlotter(plotter);
        }
    }

    @Override
    public void onRestoreInstanceState(@Nullable Parcelable in) {
        if (in instanceof Bundle) {
            final Bundle state = (Bundle) in;
            in = state.getParcelable("super");
            d3 = state.getBoolean("view.3d");
            touchListener.on3d(d3);
            renderer.restoreState(state);
        }

        super.onRestoreInstanceState(in);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable("super", super.onSaveInstanceState());
        state.putBoolean("view.3d", d3);
        renderer.saveState(state);
        return state;
    }

    @Override
    public void zoom(boolean in) {
        renderer.zoom(in);
    }

    public boolean canZoom(boolean in) {
        return renderer.canZoom(in);
    }

    @Override
    public void resetZoom() {
        renderer.resetZoom();
    }

    @Override
    public void resetCamera() {
        renderer.resetCamera();
    }

    @Override
    public void set3d(boolean d3) {
        Check.isMainThread();
        if (this.d3 != d3 || !d3Initialized) {
            this.d3Initialized = true;
            this.d3 = d3;
            if (!d3) {
                renderer.stopRotating();
                renderer.setRotationSpeed(0, 0);
                renderer.rotateTo(0, 0);
            } else {
                renderer.setRotationSpeed(0, 0.5f);
                renderer.startRotating();
            }
            touchListener.on3d(d3);
        }
    }

    @Override
    public void onDimensionChanged(@NonNull Dimensions dimensions, @Nullable Object source) {
        renderer.onDimensionsChanged(dimensions, source);
    }

    @Override
    public void onSizeChanged(@NonNull RectSize viewSize) {
        for (Listener listener : listeners) {
            listener.onSizeChanged(viewSize);
        }
    }

    @Override
    public void addListener(@NonNull Listener listener) {
        Check.isTrue(!listeners.contains(listener));
        listeners.add(listener);
    }

    @Override
    public void removeListener(@NonNull Listener listener) {
        Check.isTrue(listeners.contains(listener));
        listeners.remove(listener);
    }

    private enum TouchMode {
        PAN,
        ROTATE
    }

    private class TouchListener implements TouchHandler.Listener {

        @NonNull
        private final TouchHandler handler = TouchHandler.create(this);

        @NonNull
        private final PinchZoomTracker zoomTracker = new PinchZoomTracker(getContext());
        @NonNull
        private final PointF lastTouch = new PointF();
        @NonNull
        private final PointF cameraOffset = new PointF();

        private boolean lastTouchMoved;
        private long lastZoomTime = 0;
        @NonNull
        private TouchMode mode = TouchMode.PAN;

        @Override
        public void onTouch(float x, float y) {
        }

        @Override
        public void onTouchDown(float x, float y) {
            if (lastZoomTime != 0) {
                if (uptimeMillis() - lastZoomTime <= 50) {
                    return;
                }

                lastZoomTime = 0;
            }
            for (Listener listener : listeners) {
                listener.onTouchStarted();
            }
            renderer.stopRotating();
            lastTouch.x = x;
            lastTouch.y = y;
            cameraOffset.set(0, 0);
            lastTouchMoved = false;
            if (mode == TouchMode.PAN && plotter != null) {
                Check.isTrue(!d3);
                plotter.showCoordinates(x, y);
            }
        }

        @Override
        public void onTouchMove(float x, float y) {
            if (isZooming()) {
                return;
            }
            final float dx = x - lastTouch.x;
            final float dy = y - lastTouch.y;
            lastTouchMoved = moreThanEps(dx, dy, 3f);
            if (moreThanEps(dx, dy, 1f)) {
                cameraOffset.offset(-dx, -dy);
                switch (mode) {
                    case PAN:
                        if (plotter != null) {
                            final Dimensions.Scene scene = plotter.getSceneDimensions();
                            renderer.moveCamera(scene.toSceneDx(dx), scene.toSceneDy(dy));
                            plotter.showCoordinates(x + cameraOffset.x, y + cameraOffset.y);
                        }
                        break;
                    case ROTATE:
                        Check.isTrue(d3);
                        renderer.rotate(dy, dx);
                        break;
                }
                lastTouch.x = x;
                lastTouch.y = y;
            }
        }

        private boolean moreThanEps(float dx, float dy, float eps) {
            return dx > eps || dx < -eps || dy > eps || dy < -eps;
        }

        @Override
        public void onTouchUp(float x, float y) {
            if (isZooming()) {
                return;
            }

            final float vx;
            final float vy;
            if (lastTouchMoved) {
                vx = handler.getXVelocity() / 100f;
                vy = handler.getYVelocity() / 100f;
            } else {
                vx = 0;
                vy = 0;
            }

            switch (mode) {
                case PAN:
                    renderer.stopMovingCamera();
                    if (plotter != null) {
                        plotter.hideCoordinates();
                    }
                    break;
                case ROTATE:
                    Check.isTrue(d3);
                    renderer.setRotationSpeed(vy, vx);
                    renderer.startRotating();
                    break;
            }
        }

        private boolean isZooming() {
            return lastZoomTime != 0;
        }

        private void setZooming(boolean zooming) {
            lastZoomTime = uptimeMillis();
            renderer.setPinchZoom(zooming);
            if (d3) {
                renderer.startRotating();
            }
        }

        @Override
        public void onTouchZoomDown(float x1, float y1, float x2, float y2) {
            setZooming(true);
            zoomTracker.reset(x1, y1, x2, y2);
        }

        @Override
        public void onTouchZoomMove(float x1, float y1, float x2, float y2) {
            setZooming(true);
            final ZoomLevels levels = zoomTracker.update(x1, y1, x2, y2);
            if (levels.isChanged()) {
                zoomTracker.reset(x1, y1, x2, y2);
                renderer.zoomBy(levels);
            }
        }

        @Override
        public void onTouchZoomUp(float x1, float y1, float x2, float y2) {
            setZooming(false);
        }

        public void on3d(boolean d3) {
            mode = d3 ? TouchMode.ROTATE : TouchMode.PAN;
        }
    }
}

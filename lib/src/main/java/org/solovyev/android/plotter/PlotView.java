package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.os.SystemClock.uptimeMillis;

public class PlotView extends GLSurfaceView implements PlottingView {

	@Nullable
	private Plotter plotter;

	@Nonnull
	private final PlotRenderer renderer;

	@Nonnull
	private final TouchListener touchListener = new TouchListener();

	private boolean attached;

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

	private void init() {
		setOnTouchListener(touchListener.handler);
	}

	@Nonnull
	private static PlotRenderer initGl(@Nonnull PlotView view) {
		view.setEGLConfigChooser(new MultisampleConfigChooser());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setPreserveEGLContextOnPause(true);
		}

		final PlotRenderer renderer = new PlotRenderer(view);
		view.setRenderer(renderer);

		view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		return renderer;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (plotter != null) {
			plotter.attachView(this);
		}
		attached = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		attached = false;
		if (plotter != null) {
			plotter.detachView(this);
		}
		super.onDetachedFromWindow();
	}

	public void setPlotter(@Nonnull Plotter plotter) {
		Check.isMainThread();
		Check.isNull(this.plotter);
		this.plotter = plotter;
		this.renderer.setPlotter(plotter);
		if (attached) {
			plotter.attachView(this);
		}
	}

	@Override
	public void onRestoreInstanceState(@Nullable Parcelable in) {
		if (in instanceof Bundle) {
			final Bundle state = (Bundle) in;
			in = state.getParcelable("super");
			renderer.restoreState(state);
		}

		super.onRestoreInstanceState(in);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Bundle state = new Bundle();
		state.putParcelable("super", super.onSaveInstanceState());
		renderer.saveState(state);
		return state;
	}

	@Override
	public void zoom(boolean in) {
		renderer.zoom(in);
	}

	@Override
	public void resetZoom() {
		renderer.resetZoom();
	}

	private class TouchListener implements TouchHandler.Listener {

		@Nonnull
		private final TouchHandler handler = TouchHandler.create(this);

		@Nonnull
		private final PinchZoomTracker zoomTracker = new PinchZoomTracker();

		@Nonnull
		private final PointF lastTouch = new PointF();

		private long lastZoomTime = 0;

		@Override
		public void onTouchDown(float x, float y) {
			if (lastZoomTime != 0) {
				if (uptimeMillis() - lastZoomTime <= 50) {
					return;
				}

				lastZoomTime = 0;
			}
			renderer.stopRotating();
			lastTouch.x = x;
			lastTouch.y = y;
		}

		@Override
		public void onTouchMove(float x, float y) {
			if (isZooming()) {
				return;
			}
			final float dx = x - lastTouch.x;
			final float dy = y - lastTouch.y;
			if (dx > 1f || dx < -1f || dy > 1f || dy < -1f) {
				renderer.setRotation(dy, dx);
				lastTouch.x = x;
				lastTouch.y = y;
			}
		}

		@Override
		public void onTouchUp(float x, float y) {
			if (isZooming()) {
				return;
			}
			final float vx = handler.getXVelocity();
			final float vy = handler.getYVelocity();
			renderer.setRotation(vy / 100f, vx / 100f);
			renderer.startRotating();
		}

		private boolean isZooming() {
			return lastZoomTime != 0;
		}

		private void setZooming(boolean zooming) {
			lastZoomTime = uptimeMillis();
			renderer.setPinchZoom(zooming);
			renderer.startRotating();
		}

		@Override
		public void onTouchZoomDown(float x1, float y1, float x2, float y2) {
			setZooming(true);
			zoomTracker.reset(x1, y1, x2, y2);
		}

		@Override
		public void onTouchZoomMove(float x1, float y1, float x2, float y2) {
			setZooming(true);
			final PointF levels = zoomTracker.update(x1, y1, x2, y2);
			if (levels.x != 1f || levels.y != 1f) {
				zoomTracker.reset(x1, y1, x2, y2);
				renderer.zoomBy(levels.x > 1f ? Math.max(levels.x, levels.y) : Math.min(levels.x, levels.y));
				//renderer.zoomBy(levels.x, levels.y);
			}
		}

		@Override
		public void onTouchZoomUp(float x1, float y1, float x2, float y2) {
			setZooming(false);
		}
	}

}

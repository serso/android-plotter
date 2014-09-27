package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;

import javax.annotation.Nonnull;

public class PlotView extends GLSurfaceView implements PlotSurface {

	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

	@Nonnull
	private final PlotRenderer renderer;

	@Nonnull
	private final TouchListener touchListener = new TouchListener();

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

	public void plot(@Nonnull Function function) {
		renderer.plot(function);
	}

	public void plot(@Nonnull PlotFunction function) {
		renderer.plot(function);
	}

	public void plotNothing() {
		renderer.plotNothing();
	}

	public void setDirtyFunctions() {
		renderer.setDirtyFunctions();
	}

	private class TouchListener implements TouchHandler.Listener {

		@Nonnull
		private final TouchHandler handler = TouchHandler.create(this);

		@Nonnull
		private final PointF lastTouch = new PointF();

		@Override
		public void onTouchDown(float x, float y) {
			renderer.stopRotating();
			lastTouch.x = x;
			lastTouch.y = y;
		}

		@Override
		public void onTouchMove(float x, float y) {
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
			final float vx = handler.getXVelocity();
			final float vy = handler.getYVelocity();
			renderer.setRotation(vy / 100f, vx / 100f);
			renderer.startRotating();
		}

		@Override
		public void onTouchZoomDown(float x1, float y1, float x2, float y2) {
		}

		@Override
		public void onTouchZoomMove(float x1, float y1, float x2, float y2) {
		}

		@Override
		public void onTouchZoomUp(float x1, float y1, float x2, float y2) {
		}
	}
}

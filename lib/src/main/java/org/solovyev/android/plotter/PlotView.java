package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import javax.annotation.Nonnull;

public class PlotView extends GLSurfaceView implements PlotSurface {

	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

	@Nonnull
	private final PlotRenderer renderer;

	@Nonnull
	private final PointF lastTouch = new PointF();

	public PlotView(Context context) {
		super(context);
		renderer = init(this);
	}

	public PlotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		renderer = init(this);
	}

	@Nonnull
	private static PlotRenderer init(@Nonnull PlotView view) {
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

	@Override
	public boolean onTouchEvent(@Nonnull MotionEvent e) {
		final float x = e.getX();
		final float y = e.getY();

		final int action = e.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				renderer.stopRotating();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_MOVE:
				final float dx = x - lastTouch.x;
				final float dy = y - lastTouch.y;
				if (dx > 1f || dx < -1f || dy > 1f || dy < -1f) {
					renderer.setRotation(dy, dx);
					lastTouch.x = x;
					lastTouch.y = y;
				}
				if(action == MotionEvent.ACTION_UP) {
					renderer.startRotating();
				}
				break;
		}
		return true;
	}
}

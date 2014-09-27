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
	private final PointF previousTouch = new PointF();

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
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();

		switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:

				float dx = x - previousTouch.x;
				float dy = y - previousTouch.y;

				// reverse direction of rotation above the mid-line
				if (y > getHeight() / 2) {
					dx = dx * -1;
				}

				// reverse direction of rotation to left of the mid-line
				if (x < getWidth() / 2) {
					dy = dy * -1;
				}
				requestRender();
		}

		previousTouch.x = x;
		previousTouch.y = y;
		return true;
	}
}

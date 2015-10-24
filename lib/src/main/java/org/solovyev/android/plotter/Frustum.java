package org.solovyev.android.plotter;

import android.util.Log;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;

public final class Frustum {

	private static final String TAG = Plot.getTag("Frustum");
	/*
	View from top
											 +
							  +              |
		near   +    k*near    |    k*near    |
	+---------------------------------------------->
			   +              |              |
							  +              |
											 +
				far=near+2*k*near
	+---------------------------------------->
	 */
	// constant to calculate distance between "near" plane and scene plane (dist("near", "scene") == dist("scene", "far")),
	// multiplier of "near" (K * near)
	private static final int K = 2;
	// horizontal view angle in radians
	private static final float VIEW_ANGLE = (float) (Math.PI * 30f / 180f);
	private static final float TAN = (float) Math.tan(VIEW_ANGLE / 2f);

	// width and height of the "near" clipping plane
	@Nonnull
	private final RectSizeF nearPlane = new RectSizeF();
	@Nonnull
	private final RectSizeF scenePlane = new RectSizeF();

	private float near;
	private float far;

	private float distance;

	@Nonnull
	private Zoom zoom = Zoom.one();
	private float aspectRatio;

	private Frustum(@Nonnull Zoom zoom, float aspectRatio) {
		update(zoom, aspectRatio);
	}

	@Nonnull
	static Frustum empty() {
		return new Frustum(Zoom.one(), 1);
	}

	boolean update(@Nonnull Zoom zoom, float aspectRatio) {
		if (this.aspectRatio == aspectRatio && this.zoom.equals(zoom)) {
			return false;
		}
		this.aspectRatio = aspectRatio;
		this.zoom = zoom;

		recalculate();

		Log.d(TAG, "Frustum: near=" + near + ", far=" + far + ", distance=" + distance);
		Log.d(TAG, "Scene " + scenePlane);

		return true;

	}

	private void recalculate() {
		// we assume that:
		// 1. if zoom == 1 then width of the scene should be 1 and height = 1 * aspectRation
		// 2. far = 3 * near
		// 3. view angle is constant and equals to 60 degrees
		near = 1f / ((K + 1) * TAN);
		far = 3f * near;

		distance = 2f * near;

		nearPlane.width = 4f * near / (near + far);
		nearPlane.height = nearPlane.width * aspectRatio;

		scenePlane.width = 4f * near * TAN;
		scenePlane.height = scenePlane.width * aspectRatio;

		multiplyBy(zoom.level);
	}

	private void multiplyBy(float value) {
		near *= value;
		far *= value;
		distance *= value;
		nearPlane.multiplyBy(value);
		scenePlane.multiplyBy(value);
	}

	public void updateGl(@Nonnull GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		final float halfWidth = nearPlane.width / 2;
		final float halfHeight = nearPlane.height / 2;
		gl.glFrustumf(-halfWidth, halfWidth, -halfHeight, halfHeight, near, far);
	}

	public float distance() {
		return distance;
	}
}

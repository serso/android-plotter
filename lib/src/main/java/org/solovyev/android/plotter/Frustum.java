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
	public float width;
	public float height;

	public float near;
	public float far;

	public float distance;

	@Nonnull
	public Zoom zoom = Zoom.one();
	public float aspectRatio;

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

		final float sceneWidth = 4f * near * TAN;
		final float sceneHeight = sceneWidth * aspectRatio;
		Log.d(TAG, "Frustum: near=" + near + ", far=" + far + ", distance=" + distance);
		Log.d(TAG, "Scene: w=" + sceneWidth + ", h=" + sceneHeight);

		return true;

	}

	private void recalculate() {
		// we assume that:
		// 1. if zoom == 1 then width of the scene should be 1 and height = 1 * aspectRation
		// 2. far = 3 * near
		// 3. view angle is constant and equals to 60 degrees
		this.near = 1f / ((K + 1) * TAN);
		this.far = 3f * near;

		this.distance = 2f * near;

		this.width = 4f * near / (near + far);
		this.height = width * aspectRatio;

		multiplyBy(zoom.level);
	}

	private void multiplyBy(float value) {
		this.near *= value;
		this.far *= value;
		this.distance *= value;
		this.width *= value;
		this.height *= value;
	}

	public void updateGl(@Nonnull GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-width / 2, width / 2, -height / 2, height / 2, near, far);
	}
}

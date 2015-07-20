/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.solovyev.android.plotter;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

@SuppressWarnings("SynchronizeOnNonFinalField")
final class PlotRenderer implements GLSurfaceView.Renderer {

	@Nonnull
	private final Spf spf = new Spf();

	// lock for synchronization GL objects
	@Nonnull
	private final Object lock = new Object();

	@Nonnull
	private final Context context;

	@Nonnull
	private final PlottingView view;

	@GuardedBy("lock")
	@Nullable
	private Plotter plotter;

	@GuardedBy("lock")
	private boolean glInitialized;

	@GuardedBy("lock")
	@Nonnull
	private PointF camera = new PointF();

	@Nonnull
	private final RotationHolder rotation = new RotationHolder();

	@Nonnull
	private final ZoomerHolder zoomer = new ZoomerHolder();

	@Nonnull
	private final FaderHolder fader = new FaderHolder();

	@Nonnull
	private Rect viewDimensions = new Rect();

	@Nonnull
	private final Frustum frustum = Frustum.empty();

	private volatile boolean looping = rotation.shouldRotate();

	public PlotRenderer(@Nonnull Context context, @Nonnull PlottingView view) {
		this.context = context;
		this.view = view;
	}

	public void setPlotter(@Nonnull Plotter plotter) {
		final Zoom zoom = zoomer.current();
		synchronized (lock) {
			Check.isNull(this.plotter);
			this.plotter = plotter;
			if (!viewDimensions.isEmpty()) {
				this.plotter.updateDimensions(zoom, viewDimensions.width(), viewDimensions.height(), getCamera());
			}
		}
	}

	@Nullable
	private Plotter getPlotter() {
		// plotter might changed only from NULL to some object => if change is already visible from GL thread just use
		// the object. Otherwise, do the whole sync block thing.
		Plotter localPlotter = plotter;
		if (localPlotter != null) {
			return localPlotter;
		}
		synchronized (lock) {
			localPlotter = plotter;
		}
		return localPlotter;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		// let's use fastest perspective calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glShadeModel(GL10.GL_SMOOTH);

		tryInitGl(gl, false);
	}

	private void tryInitGl(@Nonnull GL10 gl, boolean fromFrameDraw) {
		Check.isGlThread();

		// state might be changed only from false to true. If change is already visible from GL thread there is no
		// point in obtaining the lock. If change is not visible from GL thread - let's obtain the lock and check again
		if (glInitialized) {
			return;
		}

		synchronized (lock) {
			if (glInitialized) {
				return;
			}
			if (plotter != null) {
				initGl((GL11) gl, plotter, fromFrameDraw);
			}
		}
	}

	private void initGl(@Nonnull GL11 gl, @Nonnull Plotter plotter, boolean fromFrameDraw) {
		Check.isTrue(Thread.holdsLock(lock), "Should be called from synchronized block");
		Check.isTrue(!glInitialized, "Should be not initialized");
		Check.isGlThread();

		// surface was created, let's reinitialize all meshes if needed (just to avoid reinitialization in onDrawFrame)
		plotter.initGl(gl, true);

		if (!fromFrameDraw) {
			// we must to request rendering
			view.requestRender();
		}

		glInitialized = true;
	}

	@Override
	public void onDrawFrame(GL10 gl10) {
		spf.logFrameStart();
		tryInitGl(gl10, true);

		final Plotter plotter = getPlotter();
		if (plotter != null) {
			final GL11 gl = (GL11) gl10;

			final float alpha = fader.onFrame();
			final Zoom zoom = zoomer.onFrame(gl, plotter);

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			final float cameraX;
			final float cameraY;
			synchronized (lock) {
				cameraX = camera.x;
				cameraY = camera.y;
			}

			gl.glTranslatef(cameraX, cameraY, -Dimensions.DISTANCE * zoom.level);

			rotation.onFrame(gl10);

			plotter.initGl(gl, false);

			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

			plotter.draw(gl, alpha);

			gl.glDisable(GL10.GL_BLEND);
		}

		if (looping) {
			view.requestRender();
		}
		spf.logFrameEnd();
	}

	private void initFrustum(@Nonnull GL10 gl, @Nonnull Zoom zoom) {
		Check.isGlThread();

		if (viewDimensions.isEmpty()) {
			return;
		}

		if (frustum.update(zoom, (float) viewDimensions.height() / (float) viewDimensions.width())) {
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-frustum.width / 2, frustum.width / 2, -frustum.height / 2, frustum.height / 2, frustum.near, frustum.far);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, final int width, final int height) {
		viewDimensions.set(0, 0, width, height);
		gl.glViewport(0, 0, width, height);
		zoomer.onSurfaceChanged(gl);
		final Zoom zoom = zoomer.current();

		view.post(new Runnable() {
			@Override
			public void run() {
				final Plotter plotter = getPlotter();
				if (plotter != null) {
					plotter.updateDimensions(zoom, width, height, getCamera());
				}
			}
		});
	}

	public void rotate(float dx, float dy) {
		rotation.setRotationSpeed(dx, dy);
	}

	public void rotateTo(float x, float y) {
		rotation.setRotationTo(x, y);
	}

	public void setRotationSpeed(float speedX, float speedY) {
		rotation.setRotationSpeed(speedX, speedY);
	}

	public void stopRotating() {
		looping = false;
	}

	public void startRotating() {
		synchronized (rotation) {
			final boolean shouldRotate = rotation.shouldRotate();
			loop(shouldRotate);
		}
	}

	private void loop(boolean loop) {
		if (looping != loop) {
			looping = loop;
			if (looping) {
				view.requestRender();
			}
		}
	}

	public void saveState(@Nonnull Bundle bundle) {
		Check.isMainThread();
		rotation.saveState(bundle);
		zoomer.saveState(bundle);
	}

	public void restoreState(@Nonnull Bundle bundle) {
		Check.isMainThread();
		rotation.restoreState(bundle);
		zoomer.restoreState(bundle);
		view.requestRender();
	}

	public void zoom(boolean in) {
		zoomer.zoom(in);
	}

	public void resetZoom() {
		zoomer.reset();
	}

	public void zoomBy(@Nonnull ZoomLevels level) {
		zoomer.zoomBy(level);
	}

	public void setPinchZoom(boolean pinchZoom) {
		zoomer.setPinchZoom(pinchZoom);
	}

	public void moveCamera(float dx, float dy) {
		Check.isMainThread();
		synchronized (lock) {
			camera.offset(dx, -dy);
			final Plotter plotter = getPlotter();
			if (plotter != null) {
				plotter.onCameraMoved(dx, -dy);
			}
		}
		view.requestRender();
	}

	public void stopMovingCamera() {
		Check.isMainThread();

		final Plotter plotter = getPlotter();
		if (plotter != null) {
			final Zoom zoom = zoomer.current();
			plotter.updateDimensions(zoom, viewDimensions.width(), viewDimensions.height(), getCamera());
		}
		view.requestRender();
	}

	@Nonnull
	private PointF getCamera() {
		final PointF result = new PointF();
		synchronized (lock) {
			result.set(camera);
		}
		return result;
	}

	public void resetCamera() {
		boolean changed = false;
		synchronized (lock) {
			if (camera.x != 0f || camera.y != 0f) {
				camera.set(0f, 0f);
				changed = true;
			}
		}
		if (changed) {
			stopMovingCamera();
		}
	}

	private static final class Rotation {

		private static final float MIN_ROTATION = 0.5f;
		private static final Angle DEFAULT_ANGLE = new Angle(0, 0);
		private static final Angle DEFAULT_SPEED = new Angle(0f, 0.5f);

		@Nonnull
		final Angle angle;

		@Nonnull
		final Angle speed;

		@Nonnull
		final float[] matrix;

		private Rotation() {
			angle = DEFAULT_ANGLE;
			speed = DEFAULT_SPEED;
			matrix = angle.getMatrix();
		}

		private Rotation(@Nonnull Bundle bundle) {
			angle = restoreAngle(bundle, "rotation.angle", DEFAULT_ANGLE);
			speed = restoreAngle(bundle, "rotation.speed", DEFAULT_SPEED);
			final float[] array = bundle.getFloatArray("rotation.matrix");
			matrix = array != null ? array : angle.getMatrix();
		}

		public void saveState(@Nonnull Bundle bundle) {
			bundle.putParcelable("rotation.angle", angle);
			bundle.putParcelable("rotation.speed", speed);
			bundle.putFloatArray("rotation.matrix", matrix);
		}

		@Nonnull
		private static Angle restoreAngle(@Nonnull Bundle bundle, @Nonnull String name, @Nonnull Angle def) {
			final Parcelable angle = bundle.getParcelable(name);
			if (angle instanceof Angle) {
				return (Angle) angle;
			}
			return def;
		}

		public void onFrame() {
			angle.add(speed);
			speed.rotateBy(matrix);
		}

		public boolean shouldRotate() {
			return Math.abs(speed.x) >= MIN_ROTATION || Math.abs(speed.y) >= MIN_ROTATION;
		}
	}

	private final class RotationHolder {

		/**
		 * Synchronization is done on the current instance of the field, so it's always in a good state on GL thread
		 */
		@GuardedBy("rotation")
		@Nonnull
		Rotation rotation = new Rotation();

		boolean shouldRotate() {
			synchronized (rotation) {
				return rotation.shouldRotate();
			}
		}

		void onFrame(@Nonnull GL10 gl) {
			synchronized (rotation) {
				rotation.onFrame();
				gl.glMultMatrixf(rotation.matrix, 0);
			}
		}

		void setRotationSpeed(float angleX, float angleY) {
			synchronized (rotation) {
				rotation.speed.x = angleX;
				rotation.speed.y = angleY;
			}
			view.requestRender();
		}

		void saveState(@Nonnull Bundle bundle) {
			synchronized (rotation) {
				rotation.saveState(bundle);
			}
		}

		void restoreState(@Nonnull Bundle bundle) {
			synchronized (rotation) {
				rotation = new Rotation(bundle);
				looping = rotation.shouldRotate();
			}
		}

		public void setRotationTo(float x, float y) {
			synchronized (rotation) {
				rotation.angle.x = x;
				rotation.angle.y = y;
				if (x == 0 && y == 0) {
					Matrix.setIdentityM(rotation.matrix, 0);
				} else {
					final float[] newMatrix = rotation.angle.getMatrix();
					System.arraycopy(newMatrix, 0, rotation.matrix, 0, newMatrix.length);
				}
			}
			view.requestRender();
		}
	}

	private final class FaderHolder {
		@Nonnull
		private final String TAG = Plot.getTag("Fader");

		@GuardedBy("this")
		@Nonnull
		volatile Fader fader = new Fader();

		float onFrame() {
			synchronized (this) {
				if (fader.onFrame()) {
					view.requestRender();
				}
				return fader.getAlpha();
			}
		}

		public void fadeOut() {
			synchronized (this) {
				fader.fadeOut();
			}
		}

		public void fadeIn() {
			synchronized (this) {
				fader.fadeIn();
			}
		}
	}

	private final class ZoomerHolder {

		@Nonnull
		private final String TAG = Plot.getTag("Zoomer");

		@GuardedBy("PlotRenderer.this.lock")
		Object frustumZoomer;

		@GuardedBy("this")
		@Nonnull
		volatile Zoomer zoomer = new Zoomer();

		@GuardedBy("this")
		volatile boolean pinchZoom;

		@Nonnull
		Zoom onFrame(@Nonnull GL11 gl, @Nonnull Plotter plotter) {
			final Zoom zoomLevel;
			synchronized (this) {
				if (zoomer.onFrame()) {
					synchronized (lock) {
						frustumZoomer = zoomer;
					}
					initFrustum(gl, zoomer.current().multiplyBy(Dimensions.DISTANCE));

					// if we were running and now we are stopped it's time to update the dimensions
					if (!zoomer.isZooming()) {
						if (!pinchZoom) {
							plotter.updateDimensions(zoomer.current(), viewDimensions.width(), viewDimensions.height(), getCamera());
						}
						startRotating();
					} else {
						view.requestRender();
					}
				} else {
					synchronized (lock) {
						// frustum is not initialized yet => let's do it
						if (frustumZoomer != zoomer) {
							frustumZoomer = zoomer;
							initFrustum(gl, zoomer.current().multiplyBy(Dimensions.DISTANCE));
						}
					}
				}
				zoomLevel = zoomer.current();
			}
			return zoomLevel;
		}

		void onSurfaceChanged(GL10 gl) {
			synchronized (this) {
				initFrustum(gl, zoomer.current().multiplyBy(Dimensions.DISTANCE));
			}
		}

		void saveState(@Nonnull Bundle bundle) {
			synchronized (this) {
				Log.d(TAG, "Saving state: " + zoomer);
				zoomer.saveState(bundle);
			}
		}

		void restoreState(@Nonnull Bundle bundle) {
			final Zoom zoomLevel;
			synchronized (this) {
				zoomer = new Zoomer(bundle);
				Log.d(TAG, "Restoring state: " + zoomer);
				zoomLevel = zoomer.current();
			}

			if (!viewDimensions.isEmpty()) {
				final Plotter plotter = getPlotter();
				if (plotter != null) {
					plotter.updateDimensions(zoomLevel, viewDimensions.width(), viewDimensions.height(), getCamera());
				}
			}
		}

		void zoom(boolean in) {
			synchronized (this) {
				if (zoomer.zoom(in)) {
					loop(true);
				}
				if (in) {
					Log.d(TAG, "Zooming in: " + zoomer);
				} else {
					Log.d(TAG, "Zooming out: " + zoomer);
				}
			}
		}

		void reset() {
			synchronized (this) {
				if (zoomer.reset()) {
					loop(true);
				}
				Log.d(TAG, "Resetting: " + zoomer);
			}
		}

		public void zoomBy(@Nonnull ZoomLevels levels) {
			if (!levels.isChanged()) {
				return;
			}

			final Plotter plotter = getPlotter();
			if (plotter == null) {
				return;
			}

			synchronized (this) {
				final float level = levels.getLevel();
				//final boolean zooming = plotter.is3d() ? zoomer.zoomBy(level) : zoomer.zoomBy(levels.x, levels.y);
				final boolean zooming = zoomer.zoomBy(level);
				if (zooming) {
					view.requestRender();
				}
				Log.d(TAG, "Zooming by level=" + levels + ". " + zoomer);
			}
		}

		public void setPinchZoom(boolean pinchZoom) {
			synchronized (this) {
				if (this.pinchZoom != pinchZoom) {
					this.pinchZoom = pinchZoom;
					final Plotter plotter = getPlotter();
					if (this.pinchZoom) {
						if (plotter != null) {
							plotter.hideCoordinates();
						}
						fader.fadeOut();
						Log.d(TAG, "Starting pinch zoom");
					} else {
						fader.fadeIn();
						if (plotter != null) {
							plotter.updateDimensions(zoomer.current(), viewDimensions.width(), viewDimensions.height(), getCamera());
						}
						Log.d(TAG, "Ending pinch zoom");
					}
				}
			}
		}

		@Nonnull
		private Zoom current() {
			final Zoom zoom;
			synchronized (this) {
				zoom = zoomer.current();
			}
			return zoom;
		}
	}
}
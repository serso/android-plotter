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

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;

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
	private final PlottingView view;

	@GuardedBy("lock")
	@Nullable
	private Plotter plotter;

	@GuardedBy("lock")
	private boolean glInitialized;

	@GuardedBy("dimensions")
	@Nonnull
	private Dimensions dimensions = new Dimensions();

	@Nonnull
	private final RotationHolder rotation = new RotationHolder();

	@Nonnull
	private final ZoomerHolder zoomer = new ZoomerHolder();

	private static final float DISTANCE = 15f;

	private float lastTouchX, lastTouchY;

	private int width;
	private int height;

	private volatile boolean looping = rotation.shouldRotate();

	public PlotRenderer(@Nonnull PlottingView view) {
		this.view = view;
	}

	public void setPlotter(@Nonnull Plotter plotter) {
		synchronized (lock) {
			Check.isNull(this.plotter);
			this.plotter = plotter;
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

			final float zoomLevel = zoomer.onFrame(gl);

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			gl.glTranslatef(0, 0, -DISTANCE * zoomLevel);

			rotation.onFrame(gl10);

			plotter.initGl(gl, false);
			plotter.draw(gl);
		}

		if (looping) {
			view.requestRender();
		}
		spf.logFrameEnd();
	}

	private void initFrustum(@Nonnull GL10 gl, float distance) {
		Check.isGlThread();

		if (height == 0 || width == 0) {
			return;
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		final float near = distance * (1 / 3f);
		final float far = distance * 3f;
		final float w = near / 5f;
		final float h = w * height / width;
		gl.glFrustumf(-w, w, -h, h, near, far);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		gl.glViewport(0, 0, width, height);
		zoomer.onSurfaceChanged(gl);
		//setDirty();
	}

	public void setRotation(float angleX, float angleY) {
		rotation.setRotation(angleX, angleY);
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

	private static final class Rotation {

		private static final float MIN_ROTATION = 0.5f;
		private static final Angle DEFAULT_ANGLE = new Angle(-75, 0);
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

		void setRotation(float angleX, float angleY) {
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
	}

	private final class ZoomerHolder {

		@GuardedBy("PlotRenderer.this.lock")
		Object frustumZoomer;

		@GuardedBy("zoomer")
		@Nonnull
		volatile Zoomer zoomer = new Zoomer();

		float onFrame(@Nonnull GL11 gl) {
			final float zoomLevel;
			synchronized (zoomer) {
				if (zoomer.onFrame()) {
					synchronized (lock) {
						frustumZoomer = zoomer;
					}
					initFrustum(gl, DISTANCE * zoomer.getLevel());

					// if we were running and now we are stopped it's time to update the dimensions
					if (!zoomer.isZooming()) {
						// todo serso: update dimensions
						startRotating();
					} else {
						// we must loop while zoom is zooming
						looping = true;
					}
				} else {
					synchronized (lock) {
						// frustum is not initialized yet => let's do it
						if (frustumZoomer != zoomer) {
							frustumZoomer = zoomer;
							initFrustum(gl, DISTANCE * zoomer.getLevel());
						}
					}
				}
				zoomLevel = zoomer.getLevel();
			}
			return zoomLevel;
		}

		void onSurfaceChanged(GL10 gl) {
			synchronized (zoomer) {
				initFrustum(gl, DISTANCE * zoomer.getLevel());
			}
		}

		void saveState(@Nonnull Bundle bundle) {
			synchronized (zoomer) {
				zoomer.saveState(bundle);
			}
		}

		void restoreState(@Nonnull Bundle bundle) {
			synchronized (zoomer) {
				zoomer = new Zoomer(bundle);
			}
		}

		void zoom(boolean in) {
			synchronized (zoomer) {
				if(zoomer.zoom(in)) {
					loop(true);
				}
			}
		}

		void reset() {
			synchronized (zoomer) {
				if(zoomer.reset()) {
					loop(true);
				}
			}
		}
	}
}
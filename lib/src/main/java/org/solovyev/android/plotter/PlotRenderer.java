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
import org.solovyev.android.plotter.meshes.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("SynchronizeOnNonFinalField")
final class PlotRenderer implements GLSurfaceView.Renderer {

	@Nonnull
	private final PlotSurface surface;

	@Nonnull
	private final Spf spf = new Spf();

	@Nonnull
	private final DoubleBufferGroup<FunctionGraph> functionMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final DoubleBufferGroup<Mesh> otherMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final Group<Mesh> allMeshes = ListGroup.create(Arrays.<Mesh>asList(functionMeshes, otherMeshes));

	@Nonnull
	private final List<FunctionGraph> pool = new ArrayList<FunctionGraph>();

	/**
	 * Synchronization is done on the current instance of the field, so it's always in a good state on GL thread
	 */
	@GuardedBy("rotation")
	@Nonnull
	private Rotation rotation = new Rotation();

	private static final float DISTANCE = 15f;

	private float lastTouchX, lastTouchY;

	@Nonnull
	private final Zoomer zoomer = new Zoomer();

	@Nonnull
	private PlotData plotData = PlotData.create();

	@Nonnull
	private final Dimensions dimensions = new Dimensions();

	@Nonnull
	private final Executor background = Executors.newFixedThreadPool(4, new ThreadFactory() {

		@Nonnull
		private final AtomicInteger counter = new AtomicInteger(0);

		@Override
		public Thread newThread(@Nonnull Runnable r) {
			return new Thread(r, "PlotBackground #" + counter.getAndIncrement());
		}
	});

	@Nonnull
	private final Initializer initializer = new Initializer(allMeshes);

	private int width;
	private int height;

	@Nonnull
	private final MeshConfig config = MeshConfig.create();

	private volatile boolean looping = rotation.shouldRotate();

	public PlotRenderer(@Nonnull PlotSurface surface) {
		this.surface = surface;

		final SolidCube solidCube = new SolidCube(1, 1, 1);
		solidCube.setColor(Color.BLUE.transparentCopy(0.5f));
		otherMeshes.addMesh(solidCube);
		otherMeshes.addMesh(new WireFrameCube(1, 1, 1));
		otherMeshes.addMesh(new WireFrameCube(2, 2, 2));
		/*
		otherMeshes.addMesh(new WireFramePlane(5, 5, 30, 30));
		otherMeshes.addMesh(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x + y;
			}
		}).withColor(Color.BLUE));
		otherMeshes.addMesh(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.cos(x));
			}
		}).withColor(Color.CYAN));
		otherMeshes.addMesh(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		}));
		otherMeshes.addMesh(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return -x * x - y * y;
			}
		}).withColor(Color.GREEN));
		otherMeshes.addMesh(FunctionGraph.create(new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return -x * x + y * y;
			}
		}).withColor(Color.RED));
		otherMeshes.addMesh(FunctionGraph.create(5, 5, 30, 30, new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		}));*/
		/*meshes.addMesh(FunctionGraph.create(5, 5, 30, 30, new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return (float) (Math.sin(x) + Math.sin(y));
			}
		}));
		background.execute(initializer);*/
		setDirty();
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

		// fill the background
		final int bg = plotData.axisStyle.backgroundColor;
		gl.glClearColor(Color.red(bg), Color.green(bg), Color.blue(bg), Color.alpha(bg));

		gl.glShadeModel(GL10.GL_SMOOTH);

		if (this.config.alpha) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}

		zoomer.reset();

		// surface was created, let's reinitialize all meshes if needed (just to avoid reinitialization in onDrawFrame)
		allMeshes.initGl((GL11) gl, this.config);
	}

	@Override
	public void onDrawFrame(GL10 gl10) {
		spf.logFrameStart();
		if (zoomer.onFrame()) {
			setDirty();
		}

		final GL11 gl = (GL11) gl10;
		if (zoomer.getCurrent() != zoomer.getLevel()) {
			initFrustum(gl);
			zoomer.reset();
		}
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glTranslatef(0, 0, -DISTANCE * zoomer.getLevel());

		synchronized (rotation) {
			rotation.onFrame();
			gl.glMultMatrixf(rotation.matrix, 0);
		}

		allMeshes.initGl(gl, config);
		allMeshes.draw(gl);

		if (looping) {
			surface.requestRender();
		}
		spf.logFrameEnd();
	}

	private void initFrustum(@Nonnull GL11 gl) {
		initFrustum(gl, DISTANCE * zoomer.getLevel());
	}

	private void ensureFunctionsSize() {
		Check.isMainThread();

		// for each functions we should assign mesh
		// if there are not enough meshes => create new
		// if there are too many meshes => release them
		int i = 0;
		for (PlotFunction function : plotData.functions) {
			if (i < functionMeshes.size()) {
				final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.get(i);
				final FunctionGraph mesh = dbm.getNext();
				mesh.setFunction(function.function);
				mesh.setColor(function.lineStyle.color);
			} else {
				final int poolSize = pool.size();
				final FunctionGraph mesh;
				if (poolSize > 0) {
					mesh = pool.remove(poolSize - 1);
					mesh.setFunction(function.function);
				} else {
					mesh = FunctionGraph.create(5, 5, 30, 30, function.function);
				}
				mesh.setColor(function.lineStyle.color);
				functionMeshes.add(DoubleBufferMesh.wrap(mesh));
			}
			i++;
		}

		for (int k = functionMeshes.size() - 1; k >= i; k--) {
			final DoubleBufferMesh<FunctionGraph> dbm = functionMeshes.remove(k);
			final FunctionGraph mesh = dbm.getNext();
			mesh.setFunction(Function0.ZERO);
			pool.add(mesh);
		}
	}

	private void initFrustum(@Nonnull GL10 gl, float distance) {
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
		initFrustum((GL11) gl);
		//setDirty();
	}

	public void setDirty() {
		background.execute(initializer);
	}

	public void plot(@Nonnull Function function) {
		plotData.add(function);
		setDirtyFunctions();
	}

	public void setDirtyFunctions() {
		ensureFunctionsSize();
		background.execute(initializer);
	}

	public void plot(@Nonnull PlotFunction function) {
		plotData.add(function.copy());
		setDirtyFunctions();
	}

	public void plotNothing() {
		plotData.functions.clear();
		setDirtyFunctions();
	}

	public void setRotation(float angleX, float angleY) {
		synchronized (rotation) {
			rotation.speed.x = angleX;
			rotation.speed.y = angleY;
		}
		surface.requestRender();
	}

	public void stopRotating() {
		looping = false;
	}

	public void startRotating() {
		looping = rotation.shouldRotate();
		if (looping) {
			surface.requestRender();
		}
	}

	public void saveState(@Nonnull Bundle bundle) {
		Check.isMainThread();
		synchronized (rotation) {
			rotation.saveState(bundle);
		}
	}

	public void restoreState(@Nonnull Bundle bundle) {
		Check.isMainThread();
		synchronized (rotation) {
			rotation = new Rotation(bundle);
			looping = rotation.shouldRotate();
		}
		surface.requestRender();
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
}
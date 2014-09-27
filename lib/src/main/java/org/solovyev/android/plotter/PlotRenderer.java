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
import android.opengl.Matrix;
import org.solovyev.android.plotter.meshes.*;

import javax.annotation.Nonnull;
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

final class PlotRenderer implements GLSurfaceView.Renderer {

	@Nonnull
	private final PlotSurface surface;

	@Nonnull
	private final Fps fps = new Fps();

	@Nonnull
	private final DoubleBufferGroup<FunctionGraph> functionMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final DoubleBufferGroup<Mesh> otherMeshes = DoubleBufferGroup.create();

	@Nonnull
	private final Group<Mesh> allMeshes = ListGroup.create(Arrays.<Mesh>asList(functionMeshes, otherMeshes));

	@Nonnull
	private final List<FunctionGraph> pool = new ArrayList<FunctionGraph>();

	@Nonnull
	private final Angles rotation = new Angles(0f, 0.5f);

	@Nonnull
	private final Angles angles = new Angles(-75, 0);

	@Nonnull
	private final float[] matrix;

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

	private volatile boolean rotating = rotation.shouldRotate();

	public PlotRenderer(@Nonnull PlotSurface surface) {
		this.surface = surface;
		this.matrix = angles.getMatrix();

		final SolidCube solidCube = new SolidCube(1, 1, 1);
		solidCube.setColor(Color.BLUE);
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

		zoomer.reset();

		// surface was created, let's reinitialize all meshes if needed (just to avoid reinitialization in onDrawFrame)
		allMeshes.initGl((GL11) gl, this.config);
	}

	@Override
	public void onDrawFrame(GL10 gl10) {
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

		angles.add(rotation);
		rotation.rotateBy(matrix);
		gl.glMultMatrixf(matrix, 0);

		allMeshes.initGl(gl, config);
		allMeshes.draw(gl);

		fps.logFrame();
		if (rotating) {
			surface.requestRender();
		}
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
		rotation.x = angleX;
		rotation.y = angleY;
		surface.requestRender();
	}

	public void stopRotating() {
		rotating = false;
	}

	public void startRotating() {
		rotating = rotation.shouldRotate();
		if (rotating) {
			surface.requestRender();
		}
	}

	private static final class Angles {

		private static final float MIN_ROTATION = 0.5f;

		@Nonnull
		private final float[] rotation = new float[16];

		@Nonnull
		private final float[] tmp = new float[16];

		public float x;
		public float y;

		private Angles() {
		}

		private Angles(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void add(@Nonnull Angles angles) {
			x += angles.x;
			y += angles.y;

			if (x >= 180) {
				x -= 360;
			}

			if (x < -180) {
				x += 360;
			}

			if (y >= 180) {
				y -= 360;
			}

			if (y < -180) {
				y += 360;
			}
		}

		@Nonnull
		public float[] getMatrix() {
			final float[] matrix = new float[16];
			rotateTo(matrix);
			return matrix;
		}

		public void rotateTo(float[] matrix) {
			Matrix.setIdentityM(matrix, 0);
			Matrix.rotateM(matrix, 0, x, 1, 0, 0);
			Matrix.rotateM(matrix, 0, y, 0, 1, 0);
		}

		public void rotateBy(@Nonnull float[] matrix) {
			Matrix.setIdentityM(rotation, 0);
			Matrix.rotateM(rotation, 0, x, 1, 0, 0);
			Matrix.rotateM(rotation, 0, y, 0, 1, 0);
			Matrix.multiplyMM(tmp, 0, rotation, 0, matrix, 0);
			System.arraycopy(tmp, 0, matrix, 0, 16);
		}

		public boolean shouldRotate() {
			return Math.abs(x) >= MIN_ROTATION || Math.abs(y) >= MIN_ROTATION;
		}
	}
}
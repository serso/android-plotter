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
import android.os.Build;
import org.solovyev.android.plotter.meshes.*;

import javax.annotation.Nonnull;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.List;

final class PlotRenderer implements GLSurfaceView.Renderer {

	@Nonnull
	private final PlotSurface surface;
	private final boolean highQuality = Build.VERSION.SDK_INT >= 5;

	@Nonnull
	private final List<Mesh> meshes = new ArrayList<Mesh>();

	private final float[] matrix1 = new float[16], matrix2 = new float[16], matrix3 = new float[16];
	private float angleX, angleY;
	private volatile boolean dirty = true;

	private static final float DISTANCE = 15f;

	private float lastTouchX, lastTouchY;

	@Nonnull
	private final Zoomer zoomer = new Zoomer();


	@Nonnull
	private PlotData plotData = PlotData.create();

	@Nonnull
	private final Dimensions dimensions = new Dimensions(new Dimensions.Listener() {
		@Override
		public void onChanged() {
			setDirty();
		}
	});

	@Nonnull
	private final Dimensions d = new Dimensions(new Dimensions.Listener() {
		@Override
		public void onChanged() {
		}
	});

	private int width;
	private int height;

	public PlotRenderer(@Nonnull PlotSurface surface) {
		this.surface = surface;
		Matrix.setIdentityM(matrix1, 0);
		Matrix.rotateM(matrix1, 0, -75, 1, 0, 0);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		final SolidCube solidCube = new SolidCube((GL11) gl, 1, 1, 1);
		solidCube.init((GL11) gl);
		solidCube.setColor(Color.BLUE);
		meshes.add(solidCube);

		final WireFrameCube wireFrameCube1 = new WireFrameCube((GL11) gl, 1, 1, 1);
		wireFrameCube1.init((GL11) gl);
		meshes.add(wireFrameCube1);

		final WireFrameCube wireFrameCube2 = new WireFrameCube((GL11) gl, 2, 2, 2);
		wireFrameCube2.init((GL11) gl);
		meshes.add(wireFrameCube2);

		final WireFramePlane wireFramePlane = new WireFramePlane((GL11) gl, 5, 5, 30, 30);
		wireFramePlane.init((GL11) gl);
		meshes.add(wireFramePlane);

		final FunctionGraph functionGraph = new FunctionGraph((GL11) gl, 5, 5, 30, 30, new Function2() {
			@Override
			public float evaluate(float x, float y) {
				return x * x + y * y;
			}
		});
		functionGraph.init((GL11) gl);
		meshes.add(functionGraph);

		gl.glDisable(GL10.GL_DITHER);
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		// let's use fastest perspective calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		// fill the background
		// todo serso: here we overdraw the window background. Try to use transparent background
		final int bg = plotData.axisStyle.backgroundColor;
		gl.glClearColor(Color.red(bg), Color.green(bg), Color.blue(bg), Color.alpha(bg));

		gl.glShadeModel(GL10.GL_SMOOTH);
		ensureMeshesSize((GL11) gl);
		angleX = .5f;
		angleY = 0;

		zoomer.reset();
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
		if (dirty) {
			ensureMeshesSize(gl);

			dimensions.copy(d);
			d.setGraphDimensions(dimensions.getGWidth() * zoomer.getLevel() / 4, dimensions.getGHeight() * zoomer.getLevel() / 4);

			for (Mesh mesh : meshes) {
				if (mesh instanceof Graph3d) {
					((Graph3d) mesh).update(gl, d);
				}
			}
			dirty = false;
		}

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glTranslatef(0, 0, -DISTANCE * zoomer.getLevel());
		Matrix.setIdentityM(matrix2, 0);
		float ax = Math.abs(angleX);
		float ay = Math.abs(angleY);
		if (ay * 3 < ax) {
			Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
		} else if (ax * 3 < ay) {
			Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
		} else {
			if (ax > ay) {
				Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
				Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
			} else {
				Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
				Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
			}
		}
		Matrix.multiplyMM(matrix3, 0, matrix2, 0, matrix1, 0);
		gl.glMultMatrixf(matrix3, 0);
		System.arraycopy(matrix3, 0, matrix1, 0, 16);

		for (Mesh mesh : meshes) {
			mesh.draw(gl);
		}
	}

	private void initFrustum(@Nonnull GL11 gl) {
		initFrustum(gl, DISTANCE * zoomer.getLevel());
	}

	private void ensureMeshesSize(@Nonnull GL11 gl) {
		// for each functions we should assign mesh
		// if there are not enough meshes => create new
		// if there are too many meshes => disable them
		int i = 0;
		for (PlotFunction function : plotData.functions) {
			for (; i < meshes.size(); i++) {
				final Mesh mesh = meshes.get(i);
				if (mesh instanceof Graph3d) {
					((Graph3d) mesh).setFunction(function);
					i++;
					break;
				}
			}

			if (i == meshes.size()) {
				final Graph3d mesh = new Graph3d(gl, highQuality);
				mesh.setFunction(function);
				meshes.add(mesh);
				i++;
			}
		}

		for (; i < meshes.size(); i++) {
			final Mesh mesh = meshes.get(i);
			if (mesh instanceof Graph3d) {
				((Graph3d) mesh).setFunction(null);
			}
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
		setDirty();
	}

	private void setDirty(boolean force) {
		if (!dirty || force) {
			dirty = true;
			surface.requestRender();
		}
	}

	private void setDirty() {
		setDirty(false);
	}

	public void plot(@Nonnull Function function) {
		plotData.add(function);
		setDirty();
	}

	public void plot(@Nonnull PlotFunction function) {
		plotData.add(function.copy());
		setDirty();
	}

	public void plotNothing() {
		plotData.functions.clear();
		setDirty();
	}
}
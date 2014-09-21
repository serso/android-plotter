/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.Mesh;
import org.solovyev.android.plotter.meshes.Meshes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class Graph3d implements Mesh {

	// vertices count per polygon (triangle = 3)
	public static final int VERTICES_COUNT = 3;

	// linear polygons count
	private final int n;
	// n^2
	private final int nn;
	// total polygons count
	private final int polygonsCount;
	// ticks count
	private final int ticksCount;

	private ShortBuffer indices;
	private FloatBuffer vertices;
	private FloatBuffer colors;

	private int indicesVbo;
	private int verticesVbo;
	private int colorsVbo;

	private boolean useVbo;

	@Nullable
	private PlotFunction function;

	Graph3d(@Nonnull GL11 gl, boolean useHighQuality3d) {
		n = useHighQuality3d ? 36 : 24;
		nn = n * n;
		ticksCount = useHighQuality3d ? 5 : 0;
		polygonsCount = nn + 6 + 8 + ticksCount * 6;

		short[] b = new short[nn];
		int p = 0;
		for (int i = 0; i < n; i++) {
			short v = 0;
			for (int j = 0; j < n; v += n + n, j += 2) {
				b[p++] = (short) (v + i);
				b[p++] = (short) (v + n + n - 1 - i);
			}
			v = (short) (n * (n - 2));
			i++;
			for (int j = n - 1; j >= 0; v -= n + n, j -= 2) {
				b[p++] = (short) (v + n + n - 1 - i);
				b[p++] = (short) (v + i);
			}
		}
		indices = Meshes.allocateBuffer(b);

		final String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		useVbo = extensions.contains("vertex_buffer_object");

		if (useVbo) {
			final int[] out = new int[3];
			gl.glGenBuffers(3, out, 0);
			verticesVbo = out[0];
			colorsVbo = out[1];
			indicesVbo = out[2];
		}
	}

	public void update(@Nonnull GL10 gl10, @Nonnull Dimensions dimensions) {
		if (function == null) {
			return;
		}
		final GL11 gl = (GL11) gl10;
		final SuperFunction function = this.function.function;
		final LineStyle lineStyle = this.function.lineStyle;

		// triangle polygon => 3 vertices per polygon
		final float vertices[] = new float[polygonsCount * VERTICES_COUNT];

		float maxAbsZ = fillFunctionPolygonVertices(function, dimensions, vertices);
		final float[] colors = prepareFunctionPolygonColors(lineStyle, vertices, maxAbsZ);

		int base = nn * 3;
		int colorBase = nn * 4;
		final int baseSize = 2;

		fillBasePolygonVectors(vertices, colors, base, colorBase, baseSize);

		base += 8 * 3;
		colorBase += 8 * 4;

		fillAxisPolygonVectors(vertices, colors, base, colorBase);

		base += 6 * 3;
		colorBase += 6 * 4;

		fillAxisGridPolygonVectors(ticksCount, vertices, colors, base, colorBase);

		this.vertices = Meshes.allocateBuffer(vertices);
		this.colors = Meshes.allocateBuffer(colors);

		if (useVbo) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesVbo);
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, this.vertices.capacity() * 4, this.vertices, GL11.GL_STATIC_DRAW);
			this.vertices = null;

			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorsVbo);
			gl.glBufferData(GL11.GL_ARRAY_BUFFER, this.colors.capacity(), this.colors, GL11.GL_STATIC_DRAW);
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			this.colors = null;

			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
			gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 2, indices, GL11.GL_STATIC_DRAW);
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
	}

	private void fillAxisGridPolygonVectors(int ticks, float[] vertices, float[] colors, int base, int colorBase) {
		int p = base;
		final float tick = .03f;
		final float offset = .01f;
		for (int i = 1; i <= ticks; ++i) {
			vertices[p] = i - tick;
			vertices[p + 1] = -offset;
			vertices[p + 2] = -offset;

			vertices[p + 3] = i + tick;
			vertices[p + 4] = offset;
			vertices[p + 5] = offset;
			p += 6;

			vertices[p] = -offset;
			vertices[p + 1] = i - tick;
			vertices[p + 2] = -offset;

			vertices[p + 3] = offset;
			vertices[p + 4] = i + tick;
			vertices[p + 5] = offset;
			p += 6;

			vertices[p] = -offset;
			vertices[p + 1] = -offset;
			vertices[p + 2] = i - tick;

			vertices[p + 3] = offset;
			vertices[p + 4] = offset;
			vertices[p + 5] = i + tick;
			p += 6;

		}
		for (int i = colorBase + ticks * 6 * 4 - 1; i >= colorBase; --i) {
			colors[i] = 1f;
		}
	}

	private void fillAxisPolygonVectors(float[] vertices, float[] colors, int base, int colorBase) {
		final float unit = 2;
		final float axis[] = {
				0, 0, 0,
				unit, 0, 0,
				0, 0, 0,
				0, unit, 0,
				0, 0, 0,
				0, 0, unit,
		};
		System.arraycopy(axis, 0, vertices, base, 6 * 3);
		for (int i = colorBase; i < colorBase + 6 * 4; i += 4) {
			Color.fill(colors, i, Color.WHITE);
		}
	}

	private void fillBasePolygonVectors(float[] vertices, float[] colors, int base, int colorBase, int baseSize) {
		int p = base;
		for (int i = -baseSize; i <= baseSize; i += 2 * baseSize) {
			vertices[p] = i;
			vertices[p + 1] = -baseSize;
			vertices[p + 2] = 0;
			p += 3;
			vertices[p] = i;
			vertices[p + 1] = baseSize;
			vertices[p + 2] = 0;
			p += 3;
			vertices[p] = -baseSize;
			vertices[p + 1] = i;
			vertices[p + 2] = 0;
			p += 3;
			vertices[p] = baseSize;
			vertices[p + 1] = i;
			vertices[p + 2] = 0;
			p += 3;
		}

		for (int i = colorBase; i < colorBase + 8 * Color.COMPONENTS; i += Color.COMPONENTS) {
			Color.fill(colors, i, Color.WHITE);
		}
	}

	private float fillFunctionPolygonVertices(SuperFunction f, @Nonnull Dimensions dimensions, float[] vertices) {
		final int arity = f.getArity();

		final float xMin = dimensions.getXMin();
		final float xMax = dimensions.getXMax();

		final float yMin = dimensions.getXMin();
		final float yMax = dimensions.getXMax();

		final float Δx = (xMax - xMin) / (n - 1);
		final float Δy = (yMax - yMin) / (n - 1);

		float y = yMin;
		float x = xMin - Δx;

		float maxAbsZ = 0;

		float z = 0;
		if (arity == 0) {
			z = f.evaluate();
		}

		int k = 0;
		for (int i = 0; i < n; i++, y += Δy) {
			float xinc = (i & 1) == 0 ? Δx : -Δx;

			x += xinc;

			if (arity == 1) {
				z = f.evaluate(y);
			}

			for (int j = 0; j < n; j++, x += xinc, k += VERTICES_COUNT) {

				if (arity == 2) {
					z = f.evaluate(y, x);
				}

				vertices[k] = x;
				vertices[k + 1] = y;
				vertices[k + 2] = z;

				if (!Float.isNaN(z)) {
					final float absZ = Math.abs(z);
					if (absZ > maxAbsZ) {
						maxAbsZ = absZ;
					}
				} else {
					vertices[k + 2] = 0;
				}
			}
		}

		return maxAbsZ;
	}

	private float[] prepareFunctionPolygonColors(LineStyle lineStyle, float[] vertices, float maxAbsZ) {
		// 4 color components per polygon (color[i] = red, color[i+1] = green, color[i+2] = blue, color[i+3] = alpha )
		final float colors[] = new float[polygonsCount * Color.COMPONENTS];

		final int lineColor = lineStyle.color;
		for (int i = 0, j = VERTICES_COUNT - 1; i < nn; i++, j += VERTICES_COUNT) {
			final float z = vertices[j];

			if (!Float.isNaN(z)) {
				if (lineStyle.colorType == LineStyle.ColorType.COLOR_MAP) {
					final float color = z / maxAbsZ;
					final float abs = Math.abs(color);
					Color.fillVertex(colors, i, color, 1 - abs * .3f, -color, 255f);
				} else {
					Color.fillVertex(colors, i, lineColor);
				}
			} else {
				Color.fillVertex(colors, i, Color.TRANSPARENT);
			}
		}
		return colors;
	}

	public void draw(@Nonnull GL10 gl10) {
		if (function == null) {
			return;
		}

		final GL11 gl = (GL11) gl10;
		if (useVbo) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesVbo);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);

			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorsVbo);
			gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, 0);

			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			// gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, N*N);

			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
			gl.glDrawElements(GL10.GL_LINE_STRIP, nn, GL10.GL_UNSIGNED_SHORT, 0);
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colors);
			gl.glDrawElements(GL10.GL_LINE_STRIP, nn, GL10.GL_UNSIGNED_SHORT, indices);
		}
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, nn);
		gl.glDrawArrays(GL10.GL_LINES, nn, polygonsCount - nn);
	}

	public void setFunction(@Nullable PlotFunction function) {
		this.function = function;
	}
}

package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseMesh implements Mesh {

	private static final int NULL = 0xDEADC0DE;
	private static final Color DEFAULT_COLOR = Color.WHITE;

	/**
	 * OpenGL instance associated with this mesh. This must be a instance which is used for drawing.
	 */
	@Nonnull
	protected GL11 gl;

	@Nonnull
	protected MeshConfig config;

	protected FloatBuffer vertices;
	protected int verticesCount = -1;

	protected ShortBuffer indices;
	protected int indicesCount = -1;
	@Nonnull
	protected IndicesOrder indicesOrder = IndicesOrder.TRIANGLES;

	@Nullable
	private Color color;
	protected FloatBuffer colors;
	protected int colorsCount = -1;

	/**
	 * Vertex buffer objects
	 */
	protected boolean useVbo;
	protected int verticesVbo = NULL;
	protected int indicesVbo = NULL;
	protected int colorsVbo = NULL;

	private static boolean supportsVbo(@Nonnull GL11 gl) {
		final String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains("vertex_buffer_object");
	}

	@Override
	public void init(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		this.gl = gl;
		this.config = config;

		final boolean usedVbo = useVbo;
		useVbo = this.config.useVbo && supportsVbo(gl);
		if (usedVbo) {
			gl.glDeleteBuffers(3, new int[]{verticesVbo, colorsVbo, indicesVbo}, 0);
		}

		if (useVbo) {
			final int[] out = new int[3];
			gl.glGenBuffers(3, out, 0);
			verticesVbo = out[0];
			colorsVbo = out[1];
			indicesVbo = out[2];
		} else {
			verticesVbo = NULL;
			colorsVbo = NULL;
			indicesVbo = NULL;
		}
	}

	public void draw(@Nonnull GL11 gl) {
		final boolean hasColors = colorsCount >= 0;

		// counter-clockwise winding
		gl.glFrontFace(GL10.GL_CCW);

		if (config.cullFace) {
			gl.glEnable(GL10.GL_CULL_FACE);
			gl.glCullFace(GL10.GL_BACK);
		}

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		if (hasColors) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		}

		if (!hasColors) {
			// color hasn't been set => fallback to default
			gl.glColor4f(DEFAULT_COLOR.red, DEFAULT_COLOR.green, DEFAULT_COLOR.blue, DEFAULT_COLOR.alpha);
		}

		onPreDraw(gl);
		if (useVbo) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesVbo);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);

			if (hasColors) {
				gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorsVbo);
				gl.glColorPointer(Color.COMPONENTS, GL10.GL_FLOAT, 0, 0);
			}

			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
			gl.glDrawElements(indicesOrder.glMode, indicesCount, GL10.GL_UNSIGNED_SHORT, 0);
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
			if (hasColors) {
				gl.glColorPointer(Color.COMPONENTS, GL10.GL_FLOAT, 0, colors);
			}
			gl.glDrawElements(indicesOrder.glMode, indicesCount, GL10.GL_UNSIGNED_SHORT, indices);
		}
		onPostDraw(gl);

		if (hasColors) {
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		if (config.cullFace) {
			gl.glDisable(GL10.GL_CULL_FACE);
		}
	}

	protected void onPostDraw(@Nonnull GL11 gl) {
	}

	protected void onPreDraw(@Nonnull GL11 gl) {
	}

	protected void setVertices(float[] vertices) {
		this.vertices = Meshes.allocateBuffer(vertices);
		this.verticesCount = vertices.length / 3;

		if (useVbo) {
			bindVboBuffer(this.vertices, verticesVbo, GL11.GL_ARRAY_BUFFER);
			this.vertices = null;
		}

		if (color != null) {
			setColor(color);
		}
	}

	private void bindVboBuffer(@Nonnull FloatBuffer source, int destination, int type) {
		bindVboBuffer(source, source.capacity() * Meshes.BYTES_IN_FLOAT, destination, type);
	}

	private void bindVboBuffer(@Nonnull ShortBuffer source, int destination, int type) {
		bindVboBuffer(source, source.capacity() * Meshes.BYTES_IN_SHORT, destination, type);
	}

	private void bindVboBuffer(@Nonnull Buffer source, int sourceBytes, int destination, int type) {
		gl.glBindBuffer(type, destination);
		gl.glBufferData(type, sourceBytes, source, GL11.GL_STATIC_DRAW);
		gl.glBindBuffer(type, 0);
	}

	protected void setIndices(short[] indices, @Nonnull IndicesOrder order) {
		this.indices = Meshes.allocateBuffer(indices);
		this.indicesCount = indices.length;
		this.indicesOrder = order;

		if (useVbo) {
			bindVboBuffer(this.indices, indicesVbo, GL11.GL_ELEMENT_ARRAY_BUFFER);
			this.indices = null;
		}
	}

	@Nonnull
	public final BaseMesh withColor(int color) {
		setColor(color);
		return this;
	}

	@Nonnull
	public final BaseMesh withColor(@Nonnull Color color) {
		setColor(color);
		return this;
	}

	public final void setColor(int color) {
		setColor(new Color(color));
	}

	public final void setColor(@Nonnull Color color) {
		this.color = color;
		if (verticesCount <= 0) {
			return;
		}
		final float[] colors = new float[verticesCount * Color.COMPONENTS];
		for (int i = 0; i < verticesCount; i++) {
			Color.fillVertex(colors, i, color);
		}
		setColors(colors);
	}

	protected void setColors(@Nonnull float[] colors) {
		if (verticesCount <= 0) {
			throw new IllegalStateException("Vertices must be set before setting the color");
		}
		if (verticesCount != colors.length / Color.COMPONENTS) {
			throw new IllegalStateException("Colors length must be the same as vertices length");
		}

		this.colors = Meshes.allocateBuffer(colors);
		this.colorsCount = colors.length / Color.COMPONENTS;

		if (useVbo) {
			bindVboBuffer(this.colors, colorsVbo, GL11.GL_ARRAY_BUFFER);
			this.colors = null;
		}
	}
}

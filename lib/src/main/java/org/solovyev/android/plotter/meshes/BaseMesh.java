package org.solovyev.android.plotter.meshes;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public abstract class BaseMesh implements Mesh {

	@Nonnull
	private static final String TAG = Meshes.getTag("BaseMesh");

	private static final int NULL = 0xDEADC0DE;

	/**
	 * Note that all properties of this class must be accessed from the GL thread if not stated otherwise
	 */

	/**
	 * OpenGL instance associated with this mesh. This must be a instance which is used for drawing.
	 */
	protected GL11 gl;
	protected MeshConfig config;

	private FloatBuffer vertices;
	private int verticesCount = -1;

	private ShortBuffer indices;
	private int indicesCount = -1;
	@Nonnull
	private IndicesOrder indicesOrder = IndicesOrder.TRIANGLES;

	private FloatBuffer colors;
	private int colorsCount = -1;

	private FloatBuffer textureCoordinates;
	private int textureId = -1;

	/**
	 * Vertex buffer objects
	 */
	protected boolean useVbo;
	private int verticesVbo = NULL;
	private int indicesVbo = NULL;
	private int colorsVbo = NULL;
	private int textureCoordinatesVbo = NULL;

	// can be set from any thread
	@Nonnull
	private volatile Color color = Color.WHITE;

	// can be accessed/changed from any thread
	@Nonnull
	private final StateHolder state = new StateHolder();

	private static boolean supportsVbo(@Nonnull GL11 gl) {
		final String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains("vertex_buffer_object");
	}

	@Nonnull
	public Color getColor() {
		return color;
	}

	@Nonnull
	public final State getState() {
		return state.get();
	}

	protected final void setDirty() {
		state.setDirty();
		Log.d(TAG, this + ": state=" + state);
	}

	@Override
	public final boolean init() {
		Check.isNotMainThread();
		if (!state.setIf(State.INITIALIZING, State.DIRTY)) {
			return false;
		}
		onInit();
		return state.set(State.INIT);
	}

	/**
	 * Method initializes mesh. Note that this method might be called on background thread
	 */
	protected void onInit() {
	}

	@Override
	public final boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		Check.isGlThread();

		if (this.gl == null || !this.gl.equals(gl)) {
			state.setIf(State.INIT, State.INIT_GL);
		}
		if (!state.setIf(State.INITIALIZING_GL, State.INIT)) {
			return false;
		}

		this.gl = gl;
		this.config = config;

		final boolean usedVbo = useVbo;
		useVbo = this.config.useVbo && supportsVbo(gl);
		if (usedVbo) {
			final int[] buffers = {verticesVbo, colorsVbo, indicesVbo, textureCoordinatesVbo};
			gl.glDeleteBuffers(buffers.length, buffers, 0);
		}

		if (useVbo) {
			final int[] out = new int[4];
			gl.glGenBuffers(out.length, out, 0);
			verticesVbo = out[0];
			colorsVbo = out[1];
			indicesVbo = out[2];
			textureCoordinatesVbo = out[3];
		} else {
			verticesVbo = NULL;
			colorsVbo = NULL;
			indicesVbo = NULL;
			textureCoordinatesVbo = NULL;
		}
		onInitGl(gl, config);
		return state.set(State.INIT_GL);
	}

	/**
	 * Method initializes mesh on the GL thread
	 *
	 * @param gl     gl instance
	 * @param config configuration
	 */
	protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
	}

	public final void draw(@Nonnull GL11 gl) {
		Check.isGlThread();

		if (getState() != State.INIT_GL) {
			return;
		}
		final boolean hasColors = colorsCount >= 0;
		final boolean hasTexture = hasTexture();

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
		if (hasTexture) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		if (!hasColors) {
			gl.glColor4f(color.red, color.green, color.blue, color.alpha);
		}

		onPreDraw(gl);
		if (useVbo) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, verticesVbo);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, 0);

			if (hasColors) {
				gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorsVbo);
				gl.glColorPointer(Color.COMPONENTS, GL10.GL_FLOAT, 0, 0);
			}
			if (hasTexture) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, textureCoordinatesVbo);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);
			}
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
			gl.glDrawElements(indicesOrder.glMode, indicesCount, GL10.GL_UNSIGNED_SHORT, 0);
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);

			if (hasColors) {
				gl.glColorPointer(Color.COMPONENTS, GL10.GL_FLOAT, 0, colors);
			}
			if (hasTexture) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordinates);
			}
			gl.glDrawElements(indicesOrder.glMode, indicesCount, GL10.GL_UNSIGNED_SHORT, indices);
		}
		onPostDraw(gl);

		if (hasTexture) {
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		if (hasColors) {
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		if (config.cullFace) {
			gl.glDisable(GL10.GL_CULL_FACE);
		}
	}

	private boolean hasTexture() {
		return textureId != -1;
	}

	protected void onPostDraw(@Nonnull GL11 gl) {
	}

	protected void onPreDraw(@Nonnull GL11 gl) {
	}

	@Nonnull
	@Override
	public final BaseMesh copy() {
		final BaseMesh copy = makeCopy();
		copy.color = this.color;
		return copy;
	}

	@Nonnull
	protected abstract BaseMesh makeCopy();

	protected void setVertices(float[] vertices) {
		setVertices(Meshes.allocateOrPutBuffer(vertices, this.vertices));
	}

	protected final void setVertices(@Nonnull FloatBuffer vertices) {
		Check.isGlThread();
		this.vertices = vertices;
		this.verticesCount = vertices.capacity() / 3;

		if (useVbo) {
			bindVboBuffer(this.vertices, verticesVbo, GL11.GL_ARRAY_BUFFER);
			this.vertices = null;
		}
	}

	private void bindVboBuffer(@Nonnull FloatBuffer source, int destination, int type) {
		bindVboBuffer(source, source.capacity() * Meshes.BYTES_IN_FLOAT, destination, type);
	}

	private void bindVboBuffer(@Nonnull ShortBuffer source, int destination, int type) {
		bindVboBuffer(source, source.capacity() * Meshes.BYTES_IN_SHORT, destination, type);
	}

	private void bindVboBuffer(@Nonnull Buffer source, int sourceBytes, int destination, int type) {
		if (destination != NULL) {
			final int[] buffers = {destination};
			gl.glDeleteBuffers(buffers.length, buffers, 0);
		}
		gl.glBindBuffer(type, destination);
		gl.glBufferData(type, sourceBytes, source, GL11.GL_STATIC_DRAW);
		gl.glBindBuffer(type, 0);
	}

	protected final void setIndices(short[] indices, @Nonnull IndicesOrder order) {
		setIndices(Meshes.allocateOrPutBuffer(indices, this.indices), order);
	}

	protected final void setIndices(@Nonnull ShortBuffer indices, @Nonnull IndicesOrder order) {
		Check.isGlThread();
		this.indices = indices;
		this.indicesCount = indices.capacity();
		this.indicesOrder = order;

		if (useVbo) {
			bindVboBuffer(this.indices, indicesVbo, GL11.GL_ELEMENT_ARRAY_BUFFER);
			this.indices = null;
		}
	}

	private void setTextureCoordinates(float[] textureCoordinates) {
		Check.isGlThread();
		this.textureCoordinates = Meshes.allocateOrPutBuffer(textureCoordinates, this.textureCoordinates);
		if (useVbo) {
			bindVboBuffer(this.textureCoordinates, textureCoordinatesVbo, GL11.GL_ARRAY_BUFFER);
			this.textureCoordinates = null;
		}
	}

	protected final void loadTexture(@Nonnull GL10 gl, @Nonnull Bitmap bitmap) {
		final int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);

		float textureCoordinates[] = {
				0.0f, 1.0f,
				1.0f, 1.0f,
				0.0f, 0.0f,
				1.0f, 0.0f};
		setTexture(textures[0], textureCoordinates);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	}

	protected final void setTexture(int textureId, float[] textureCoordinates) {
		Check.isGlThread();
		this.textureId = textureId;
		setTextureCoordinates(textureCoordinates);
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
		setColor(Color.create(color));
	}

	public final void setColor(@Nonnull Color color) {
		// todo serso: we must null colors here and call setDirty if they were not null
		this.color = color;
	}

	protected void setColors(@Nonnull float[] colors) {
		Check.isGlThread();
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

	private static final class StateHolder {
		@GuardedBy("this")
		@Nonnull
		private State state = State.DIRTY;

		@GuardedBy("this")
		@Nullable
		private State delayedState;

		@Nonnull
		public State get() {
			synchronized (this) {
				return delayedState != null ? delayedState : state;
			}
		}

		private boolean setIf(@Nonnull State newState, @Nonnull State oldState) {
			synchronized (this) {
				if (state != oldState) {
					return false;
				}
				state = newState;
			}
			return true;
		}

		public void setDirty() {
			synchronized (this) {
				if (state == State.INITIALIZING || state == State.INITIALIZING_GL) {
					// if we are in the middle of initialization process we should postpone setting dirty state until
					// the process is done
					delayedState = State.DIRTY;
				} else {
					state = State.DIRTY;
				}
			}
		}

		public boolean set(@Nonnull State newState) {
			synchronized (this) {
				if (delayedState != null) {
					state = delayedState;
					delayedState = null;
					return false;
				} else {
					state = newState;
					return true;
				}
			}
		}

		@Override
		public String toString() {
			return state.toString() + "(" + String.valueOf(delayedState) + ")";
		}
	}

	static class Swapper<M extends BaseMesh> implements DoubleBufferMesh.Swapper<M> {
		@Override
		public void swap(@Nonnull M current, @Nonnull M next) {
			next.setColor(((BaseMesh) current).color);
		}
	}
}

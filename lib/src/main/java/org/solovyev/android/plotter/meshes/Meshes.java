package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Plot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class Meshes {

	public static final int BYTES_IN_FLOAT = 4;
	public static final int BYTES_IN_SHORT = 2;

	private Meshes() {
	}

	@Nonnull
	static String getTag() {
		return Plot.getTag("Meshes");
	}

	@Nonnull
	static String getTag(@Nonnull String tag) {
		return getTag() + "/" + tag;
	}

	@Nonnull
	public static FloatBuffer allocateBuffer(float[] array) {
		return allocateBuffer(array, 0, array.length);
	}

	@Nonnull
	public static FloatBuffer allocateBuffer(float[] array, int start, int length) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(length * BYTES_IN_FLOAT);
		buffer.order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer = buffer.asFloatBuffer();
		return putBuffer(array, start, length, floatBuffer);
	}

	@Nonnull
	public static FloatBuffer putBuffer(float[] array, int start, int length, @Nonnull FloatBuffer to) {
		if (to.capacity() != length) {
			throw new IllegalArgumentException("Arrays should have save size");
		}
		to.position(0);
		to.put(array, start, length);
		to.position(0);
		return to;
	}

	@Nonnull
	static FloatBuffer allocateOrPutBuffer(@Nonnull float[] array, int start, int length, @Nullable FloatBuffer buffer) {
		FloatBuffer newBuffer;
		if (buffer != null && buffer.capacity() == length) {
			newBuffer = putBuffer(array, start, length, buffer);
		} else {
			newBuffer = allocateBuffer(array, start, length);
		}
		return newBuffer;
	}

	@Nonnull
	static FloatBuffer allocateOrPutBuffer(@Nonnull float[] array, @Nullable FloatBuffer buffer) {
		return allocateOrPutBuffer(array, 0, array.length, buffer);
	}

	@Nonnull
	public static ShortBuffer allocateBuffer(short[] array, int start, int length) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(length * BYTES_IN_SHORT);
		buffer.order(ByteOrder.nativeOrder());
		final ShortBuffer shortBuffer = buffer.asShortBuffer();
		return putBuffer(array, start, length, shortBuffer);
	}

	@Nonnull
	public static ShortBuffer putBuffer(short[] array, int start, int length, @Nonnull ShortBuffer to) {
		if (to.capacity() != length) {
			throw new IllegalArgumentException("Arrays should have save size");
		}
		to.position(0);
		to.put(array, start, length);
		to.position(0);
		return to;
	}

	@Nonnull
	static ShortBuffer allocateOrPutBuffer(@Nonnull short[] array, @Nullable ShortBuffer buffer) {
		return allocateOrPutBuffer(array, 0, array.length, buffer);
	}

	@Nonnull
	static ShortBuffer allocateOrPutBuffer(@Nonnull short[] array, int start, int length, @Nullable ShortBuffer buffer) {
		ShortBuffer newBuffer;
		if (buffer != null && buffer.capacity() == length) {
			newBuffer = putBuffer(array, start, length, buffer);
		} else {
			newBuffer = allocateBuffer(array, start, length);
		}
		return newBuffer;
	}
}

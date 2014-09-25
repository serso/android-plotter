package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
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
	public static FloatBuffer allocateBuffer(float[] array) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length * BYTES_IN_FLOAT);
		buffer.order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer = buffer.asFloatBuffer();
		return putBuffer(array, floatBuffer);
	}

	@Nonnull
	public static FloatBuffer putBuffer(float[] array, @Nonnull FloatBuffer to) {
		if (to.capacity() != array.length) {
			throw new IllegalArgumentException("Arrays should have save size");
		}
		to.position(0);
		to.put(array);
		to.position(0);
		return to;
	}

	@Nonnull
	public static ShortBuffer allocateBuffer(short[] array) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length * BYTES_IN_SHORT);
		buffer.order(ByteOrder.nativeOrder());
		final ShortBuffer shortBuffer = buffer.asShortBuffer();
		return putBuffer(array, shortBuffer);
	}

	@Nonnull
	public static ShortBuffer putBuffer(short[] array, @Nonnull ShortBuffer to) {
		if (to.capacity() != array.length) {
			throw new IllegalArgumentException("Arrays should have save size");
		}
		to.position(0);
		to.put(array);
		to.position(0);
		return to;
	}
}

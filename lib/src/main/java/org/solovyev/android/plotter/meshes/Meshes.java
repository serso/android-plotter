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
		floatBuffer.put(array);
		floatBuffer.position(0);
		return floatBuffer;
	}

	@Nonnull
	public static ShortBuffer allocateBuffer(short[] array) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length * BYTES_IN_SHORT);
		buffer.order(ByteOrder.nativeOrder());
		final ShortBuffer shortBuffer = buffer.asShortBuffer();
		shortBuffer.put(array);
		shortBuffer.position(0);
		return shortBuffer;
	}

	@Nonnull
	static ByteBuffer allocateBuffer(byte[] array) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(array.length);
		buffer.order(ByteOrder.nativeOrder());
		buffer.put(array);
		buffer.position(0);
		return buffer;
	}
}

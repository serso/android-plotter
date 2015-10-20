package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.util.Log;

import org.solovyev.android.plotter.Check;

import javax.annotation.Nonnull;

class Graph {

	float step = -1f;
	final PointF center = new PointF();

	int capacity = 4 * 3;

	int start;
	int end;

	@Nonnull
	float[] vertices = new float[capacity];

	@Nonnull
	private short[] indices = new short[capacity / 3];
	{
		initIndices();
	}

	private Graph() {
		init();
	}

	@Nonnull
	static Graph create() {
		return new Graph();
	}

	void prepend(float x, float y) {
		ensureCanPrepend();

		vertices[--start] = 0;
		vertices[--start] = y;
		vertices[--start] = x;
	}

	private void ensureCanPrepend() {
		if (start < 3) {
			makeSpaceAtTheStart();
			logCapacity();
		}
	}

	void append(float x, float y) {
		ensureCanAppend();

		vertices[end++] = x;
		vertices[end++] = y;
		vertices[end++] = 0;
	}

	private void ensureCanAppend() {
		if (end + 3 > capacity) {
			makeSpaceAtTheEnd();
			logCapacity();
		}
	}

	private void logCapacity() {
		Log.d(Meshes.getTag("Graph"), "Capacity=" + capacity);
	}

	void makeSpaceAtTheEnd() {
		if (start > capacity / 2) {
			final int length = length();
			System.arraycopy(vertices, start, vertices, 0, length);
			start = 0;
			end = length;
			return;
		}

		final int newCapacity = newCapacity(capacity);
		final float[] newVertices = new float[newCapacity];
		System.arraycopy(vertices, start, newVertices, start, length());
		vertices = newVertices;
		capacity = newCapacity;
	}

	private static int newCapacity(int capacity) {
		return 5 * capacity / 4 + 4;
	}

	public boolean canGrow(int maxCapacity) {
		return newCapacity(capacity) < maxCapacity;
	}

	private void makeSpaceAtTheStart() {
		if (end != 0 && end < capacity / 2) {
			final int newStart = start + capacity - end;
			final int length = length();
			System.arraycopy(vertices, start, vertices, newStart, length);
			start = newStart;
			end = newStart + length;
			return;
		}

		final int newCapacity = newCapacity(capacity);
		final float[] newVertices = new float[newCapacity];
		final int offset = (newCapacity - capacity) / 2;
		System.arraycopy(vertices, start, newVertices, start + offset, length());
		start += offset;
		end += offset;
		vertices = newVertices;
		capacity = newCapacity;
	}


	int length() {
		return end - start;
	}

	boolean isEmpty() {
		return start == end;
	}

	void clear() {
		init();
		Log.d(Meshes.getTag("Graph"), "Cleared");
	}

	private void init() {
		// starting not from zero to allow prepend work without resizing the array
		start = capacity / 3;
		end = capacity / 3;
	}

	public float x(int position) {
		return vertices[position];
	}

	public void moveStartTo(float x) {
		checkIsNotEmpty();
		while (start < end && vertices[start] < x) {
			start += 3;
		}

		if (start > end) {
			start = end;
		}
	}

	private void checkIsNotEmpty() {
		Check.isTrue(!isEmpty(), "Should not be empty");
	}

	public void moveEndTo(float x) {
		checkIsNotEmpty();
		while (start < end && vertices[end - 3] > x) {
			end -= 3;
		}

		if (start > end) {
			end = start;
		}
	}

	public float xMin() {
		return vertices[start];
	}

	public float xMax() {
		return vertices[end - 3];
	}

	short[] getIndices() {
		final int indicesCountNew = getIndicesCount();
		final int indicesCountOld = indices.length;
		if (indicesCountOld < indicesCountNew) {
			indices = new short[indicesCountNew];
			initIndices();
		}
		return indices;
	}

	private void initIndices() {
		for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
        }
	}

	int getIndicesCount() {
		return length() / 3;
	}
}

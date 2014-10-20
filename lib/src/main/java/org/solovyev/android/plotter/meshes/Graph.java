package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;

class Graph {

	float accuracy = 1f;

	int start = 0;
	int end = 0;

	int capacity = 4 * 3;

	@Nonnull
	float[] vertices = new float[capacity];

	@Nonnull
	private short[] indices = new short[capacity / 3];

	private Graph() {
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
		}
	}

	void makeSpaceAtTheEnd() {
		if (start > capacity / 2) {
			final int length = length();
			System.arraycopy(vertices, start, vertices, 0, length);
			start = 0;
			end = length;
			return;
		}

		final int newCapacity = 5 * capacity / 4;
		final float[] newVertices = new float[newCapacity];
		System.arraycopy(vertices, start, newVertices, start, length());
		vertices = newVertices;
		capacity = newCapacity;
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

		final int newCapacity = 5 * capacity / 4;
		final float[] newVertices = new float[newCapacity];
		final int offset = newCapacity - capacity;
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
		start = 0;
		end = 0;
	}

	public float x(int position) {
		return vertices[position];
	}

	public void moveStartTo(float x) {
		while (start < end && vertices[start] < x) {
			start += 3;
		}

		if (start > end) {
			start = end;
		}
	}

	public void moveEndTo(float x) {
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
		final int indicesCount = getIndicesCount();
		if (indices.length < indicesCount) {
			indices = new short[indicesCount];
		}
		for (short i = 0; i < indicesCount; i++) {
			indices[i] = i;
		}
		return indices;
	}

	int getIndicesCount() {
		return length() / 3;
	}
}

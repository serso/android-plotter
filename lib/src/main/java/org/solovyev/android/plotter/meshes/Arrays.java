package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

final class Arrays {

	float[] vertices;
	short[] indices;

	int vertex = 0;
	int index = 0;

	public Arrays() {
	}

	public Arrays(int verticesCount, int indicesCount) {
		this.vertices = new float[verticesCount];
		this.indices = new short[indicesCount];
	}

	public boolean isCreated() {
		return vertices != null && indices != null;
	}

	@Nonnull
	public FloatBuffer getVerticesBuffer(@Nullable FloatBuffer verticesBuffer) {
		Check.isTrue(isCreated(), "Arrays should be initialized");
		return Meshes.allocateOrPutBuffer(vertices, verticesBuffer);
	}

	@Nonnull
	public ShortBuffer getIndicesBuffer(@Nullable ShortBuffer indicesBuffer) {
		Check.isTrue(isCreated(), "Arrays should be initialized");
		return Meshes.allocateOrPutBuffer(indices, indicesBuffer);
	}

	public void add(int i, float x, float y, float z) {
		add((short) i, x, y, z);
	}

	public void add(short i, float x, float y, float z) {
		Check.isTrue(vertex < vertices.length, "Vertices must be allocated properly");
		Check.isTrue(index < indices.length, "Indices must be allocated properly");

		indices[index++] = i;
		vertices[vertex++] = x;
		vertices[vertex++] = y;
		vertices[vertex++] = z;
	}

	public void init() {
		index = 0;
		vertex = 0;
	}

	public void init(int verticesCount, int indicesCount) {
		if (vertices == null || vertices.length != verticesCount) {
			vertices = new float[verticesCount];
		}

		if (indices == null || indices.length != indicesCount) {
			indices = new short[indicesCount];
		}

		init();
	}
}

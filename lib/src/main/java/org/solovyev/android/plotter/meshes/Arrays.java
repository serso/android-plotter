package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

final class Arrays {

    float[] vertices;
    short[] indices;
    int vertex = 0;
    int index = 0;
    // create on the background thread and accessed from GL thread
    private volatile FloatBuffer verticesBuffer;
    private volatile ShortBuffer indicesBuffer;

    public Arrays() {
    }

    public Arrays(int verticesCount, int indicesCount) {
        this.vertices = new float[verticesCount];
        this.indices = new short[indicesCount];
    }

    public boolean isCreated() {
        return vertices != null && indices != null;
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
        vertex = 0;
        index = 0;
        verticesBuffer = null;
        indicesBuffer = null;
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

    public void createBuffers() {
        Check.isTrue(isCreated(), "Arrays should be initialized");
        verticesBuffer = Meshes.allocateOrPutBuffer(vertices, verticesBuffer);
        indicesBuffer = Meshes.allocateOrPutBuffer(indices, indicesBuffer);
    }

    @NonNull
    public FloatBuffer getVerticesBuffer() {
        Check.isTrue(isCreated(), "Arrays should be initialized");
        Check.isNotNull(verticesBuffer);
        return verticesBuffer;
    }

    @NonNull
    public ShortBuffer getIndicesBuffer() {
        Check.isTrue(isCreated(), "Arrays should be initialized");
        Check.isNotNull(indicesBuffer);
        return indicesBuffer;
    }
}

package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.util.Log;

import org.solovyev.android.plotter.Check;

import javax.annotation.Nonnull;

class Graph {

    final PointF center = new PointF();
    float step = -1f;
    int capacity = 4 * 3;

    int start;
    int end;

    @Nonnull
    float[] vertices = new float[capacity];

    private int indicesCount = capacity / 3;
    @Nonnull
    private short[] indices = new short[indicesCount];

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

    private static int newCapacity(int capacity) {
        return 5 * capacity / 4 + 4;
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
        final int indicesCountOld = indicesCount;
        indicesCount = getIndicesCount();
        if (indicesCountOld < indicesCount) {
            indices = new short[indicesCount];
            initIndices();
        }
        return indices;
    }

    short[] getIndices(float minY, float maxY) {
        final int verticesCount = length() / 3;
        final int indicesCount = 2 * verticesCount - 2;
        this.indicesCount = indicesCount;
        if (indices.length < indicesCount) {
            indices = new short[indicesCount];
        }
        short j = 0;
        for (short vertex = 0; vertex < verticesCount - 1; vertex++) {
            final float y = vertices[start + 3 * vertex + 1];
            final float yNext = vertices[start + 3 * (vertex + 1) + 1];
            if (y > maxY || yNext > maxY) {
                this.indicesCount -= 2;
                continue;
            } else if (y < minY || yNext < minY) {
                this.indicesCount -= 2;
                continue;
            }
            indices[j++] = vertex;
            indices[j++] = (short) (vertex + 1);
        }
        return indices;
    }

    private void initIndices() {
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
        }
    }

    int getIndicesCount() {
        return indicesCount;
    }
}

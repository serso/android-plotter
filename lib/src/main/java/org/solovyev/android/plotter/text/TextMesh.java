package org.solovyev.android.plotter.text;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.arrays.FloatArray;
import org.solovyev.android.plotter.arrays.ShortArray;
import org.solovyev.android.plotter.meshes.IndicesOrder;

import java.util.List;

public final class TextMesh {
    @NonNull
    public final ShortArray indices;
    public final IndicesOrder indicesOrder = IndicesOrder.TRIANGLES;
    @NonNull
    public final FloatArray vertices;
    @NonNull
    public final FloatArray textureCoordinates;
    @NonNull
    private final RectF bounds = new RectF();
    public int size;

    public TextMesh(int size) {
        Check.isTrue(size > 0);
        this.size = size;
        this.indices = new ShortArray(sizeToIndices(size));
        this.vertices = new FloatArray(sizeToVertices(size));
        this.textureCoordinates = new FloatArray(sizeToTextureCoordinates(size));
    }

    private static int sizeToTextureCoordinates(int size) {
        return size * 4 * 2;
    }

    private static int sizeToVertices(int size) {
        return size * 4 * 3;
    }

    private static int sizeToIndices(int size) {
        return size * 6;
    }

    @NonNull
    static TextMesh createForFullAtlas(@NonNull FontAtlas atlas, float x, float y, float z, int size) {
        final TextMesh mesh = atlas.obtainMesh(1);
        mesh.fill(x, y, z, size, size);
        atlas.setTextureCoordinates(mesh.textureCoordinates);
        return mesh;
    }

    @NonNull
    static TextMesh createForChar(@NonNull FontAtlas atlas, char c, float x, float y, float z, float width, float height) {
        final TextMesh mesh = atlas.obtainMesh(1);
        mesh.fill(x, y, z, width, height);
        atlas.setTextureCoordinates(c, mesh.textureCoordinates);
        return mesh;
    }

    private void setSize(int size) {
        Check.isTrue(size > this.size);
        this.size = size;
        this.indices.allocate(sizeToIndices(size));
        this.vertices.allocate(sizeToVertices(size));
        this.textureCoordinates.allocate(sizeToTextureCoordinates(size));
    }

    private void fill(float x, float y, float z, float width, float height) {
        Check.isTrue(size == 1);
        indices.array[0] = 0;
        indices.array[1] = 1;
        indices.array[2] = 2;
        indices.array[3] = 1;
        indices.array[4] = 3;
        indices.array[5] = 2;
        indices.size = 6;

        vertices.array[0] = x;
        vertices.array[1] = y;
        vertices.array[2] = z;
        vertices.array[3] = x + width;
        vertices.array[4] = y;
        vertices.array[5] = z;
        vertices.array[6] = x;
        vertices.array[7] = y + height;
        vertices.array[8] = z;
        vertices.array[9] = x + width;
        vertices.array[10] = y + height;
        vertices.array[11] = z;
        vertices.size = 12;

        union(bounds);
    }

    @NonNull
    public RectF getBounds() {
        return bounds;
    }

    @NonNull
    private RectF union(@NonNull RectF bounds) {
        for (int i = 0; i < vertices.size; i += 3) {
            final float x = vertices.array[i];
            bounds.left = Math.min(bounds.left, x);
            bounds.right = Math.max(bounds.right, x);
            final float y = vertices.array[i + 1];
            bounds.top = Math.min(bounds.top, y);
            bounds.bottom = Math.max(bounds.bottom, y);
        }
        return bounds;
    }

    public void translate(float dx, float dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        for (int i = 0; i < vertices.size; i += 3) {
            vertices.array[i] += dx;
            vertices.array[i + 1] += dy;
        }
        getBounds().offset(dx, dy);
    }

    public void merge(@NonNull List<TextMesh> meshes, boolean centerX, boolean centerY) {
        for (int i = 0; i < meshes.size(); i++) {
            final TextMesh mesh = meshes.get(i);
            if (centerX || centerY) {
                mesh.union(bounds);
            }
        }
        for (int i = 0; i < meshes.size(); i++) {
            merge(meshes.get(i), centerX, centerY);
        }
    }

    public void merge(@NonNull TextMesh mesh) {
        merge(mesh, false, false);
    }

    private void merge(@NonNull TextMesh mesh, boolean centerX, boolean centerY) {
        if (indices.array.length < indices.size + mesh.indices.size) {
            final int missing = (indices.size + mesh.indices.size - indices.size) / 6 + 1;
            setSize((int) Math.max(1.5 * this.size, this.size + missing));
        }
        for (int i = 0; i < mesh.indices.size; i++) {
            indices.array[indices.size + i] = (short) (mesh.indices.array[i] + vertices.size / 3);
        }
        indices.size += mesh.indices.size;

        if (centerX || centerY) {
            final float dx = centerX ? Math.abs(bounds.right - bounds.left) / 2 : 0f;
            final float dy = centerY ? Math.abs(bounds.top - bounds.bottom) / 2 : 0f;
            for (int i = 0; i < mesh.vertices.size; i += 3) {
                vertices.array[vertices.size + i] = mesh.vertices.array[i] - dx;
                vertices.array[vertices.size + i + 1] = mesh.vertices.array[i + 1] - dy;
                vertices.array[vertices.size + i + 2] = mesh.vertices.array[i + 2];
            }
            vertices.size += mesh.vertices.size;
        } else {
            vertices.append(mesh.vertices);
        }
        textureCoordinates.append(mesh.textureCoordinates);
    }

    public void reset() {
        bounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        indices.truncate(0);
        vertices.truncate(0);
        textureCoordinates.truncate(0);
    }
}

package org.solovyev.android.plotter.text;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

final class TextMeshesPool {

    private static final int MAX_MESH_SIZE = 10;
    @NonNull
    private List<List<TextMesh>> pool = new ArrayList<>();

    {
        for (int i = 0; i < MAX_MESH_SIZE; i++) {
            pool.add(new ArrayList<TextMesh>());
        }
    }

    @NonNull
    public synchronized TextMesh obtain(int meshSize) {
        final TextMesh mesh;
        if (meshSize >= MAX_MESH_SIZE) {
            return new TextMesh(meshSize);
        }
        final List<TextMesh> list = pool.get(meshSize);
        final int size = list.size();
        if (size == 0) {
            mesh = new TextMesh(meshSize);
        } else {
            mesh = list.remove(size - 1);
        }
        return mesh;
    }

    public synchronized void release(@NonNull TextMesh mesh) {
        if (mesh.size >= MAX_MESH_SIZE) {
            return;
        }
        final List<TextMesh> list = pool.get(mesh.size);
        list.add(mesh);
        mesh.reset();
    }
}

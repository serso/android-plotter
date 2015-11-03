package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import java.util.Iterator;

import javax.microedition.khronos.opengles.GL11;

public final class DoubleBufferGroup<M extends Mesh> implements Group<DoubleBufferMesh<M>> {

    @NonNull
    private final ListGroup<DoubleBufferMesh<M>> group = ListGroup.create();

    @Nullable
    private final DoubleBufferMesh.Swapper<M> swapper;

    private DoubleBufferGroup(@NonNull DoubleBufferMesh.Swapper<M> swapper) {
        this.swapper = swapper;
    }

    @NonNull
    public static <M extends Mesh> DoubleBufferGroup<M> create(@Nullable DoubleBufferMesh.Swapper<M> swapper) {
        return new DoubleBufferGroup<M>(swapper);
    }

    @Override
    public int size() {
        return group.size();
    }

    @Override
    public void clear() {
        group.clear();
    }

    @Override
    public boolean add(@NonNull DoubleBufferMesh<M> mesh) {
        return group.add(mesh);
    }

    public boolean addMesh(@NonNull M mesh) {
        return add(DoubleBufferMesh.wrap(mesh, swapper));
    }

    @Override
    @NonNull
    public DoubleBufferMesh<M> remove(int i) {
        return group.remove(i);
    }

    @NonNull
    public DoubleBufferMesh<M> get(int location) {
        return group.get(location);
    }

    @Override
    public void draw(@NonNull GL11 gl) {
        group.draw(gl);
    }

    @NonNull
    @Override
    public ListGroup<DoubleBufferMesh<M>> copy() {
        return group.copy();
    }

    @NonNull
    @Override
    public State getState() {
        return group.getState();
    }

    @Override
    public void setAlpha(float alpha) {
        group.setAlpha(alpha);
    }

    @Override
    public boolean setColor(@NonNull Color color) {
        return group.setColor(color);
    }

    @NonNull
    @Override
    public Color getColor() {
        return group.getColor();
    }

    @Override
    public boolean setWidth(int width) {
        return group.setWidth(width);
    }

    @Override
    public int getWidth() {
        return group.getWidth();
    }

    @Override
    public boolean init() {
        return group.init();
    }

    @Override
    public boolean initGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        return group.initGl(gl, config);
    }

    @Override
    public Iterator<DoubleBufferMesh<M>> iterator() {
        return group.iterator();
    }
}

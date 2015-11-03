package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;

@ThreadSafe
public final class ListGroup<M extends Mesh> implements Group<M> {

    @NonNull
    private final CopyOnWriteArrayList<M> list;

    private ListGroup() {
        list = new CopyOnWriteArrayList<M>();
    }

    private ListGroup(@NonNull CopyOnWriteArrayList<M> list) {
        this.list = list;
    }

    @NonNull
    public static <M extends Mesh> ListGroup<M> create() {
        return new ListGroup<M>();
    }

    @NonNull
    public static <M extends Mesh> ListGroup<M> create(@NonNull Collection<M> meshes) {
        return new ListGroup<M>(new CopyOnWriteArrayList<M>(meshes));
    }

    @Override
    public void draw(@NonNull GL11 gl) {
        for (M mesh : list) {
            mesh.draw(gl);
        }
    }

    @NonNull
    @Override
    public ListGroup<M> copy() {
        final CopyOnWriteArrayList<M> meshes = new CopyOnWriteArrayList<M>();
        for (M mesh : list) {
            meshes.add((M) mesh.copy());
        }
        return new ListGroup<M>(meshes);
    }

    @NonNull
    @Override
    public State getState() {
        State state = State.INIT_GL;
        for (Mesh mesh : list) {
            final State meshState = mesh.getState();
            if (state.order > meshState.order) {
                state = meshState;
            }
        }
        return state;
    }

    @Override
    public void setAlpha(float alpha) {
        for (M mesh : list) {
            mesh.setAlpha(alpha);
        }
    }

    @Override
    public boolean setColor(@NonNull Color color) {
        boolean changed = false;
        for (M mesh : list) {
            changed |= mesh.setColor(color);
        }
        return changed;
    }

    @NonNull
    @Override
    public Color getColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setWidth(int width) {
        boolean changed = false;
        for (M mesh : list) {
            changed |= mesh.setWidth(width);
        }
        return changed;
    }

    @Override
    public int getWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(@NonNull M mesh) {
        return list.add(mesh);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean init() {
        boolean changed = false;
        for (M mesh : list) {
            changed |= mesh.init();
        }
        return changed;
    }

    @Override
    public boolean initGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        boolean changed = false;
        for (M mesh : list) {
            changed |= mesh.initGl(gl, config);
        }
        return changed;
    }

    @Override
    @NonNull
    public M get(int location) {
        return list.get(location);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    @NonNull
    public M remove(int i) {
        return list.remove(i);
    }

    @Override
    public Iterator<M> iterator() {
        return list.iterator();
    }
}

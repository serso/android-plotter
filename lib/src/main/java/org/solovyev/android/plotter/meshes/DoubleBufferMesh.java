package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import javaz.annotation.concurrent.GuardedBy;
import javaz.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;

@ThreadSafe
public class DoubleBufferMesh<M extends Mesh> implements Mesh {

    @NonNull
    private static final String TAG = Meshes.getTag("DoubleBufferMesh");
    @NonNull
    private final Object lock = new Object();
    @NonNull
    private final M first;
    @NonNull
    private final M second;
    @Nullable
    private final Swapper<? super M> swapper;
    @GuardedBy("lock")
    private M current;
    @GuardedBy("lock")
    private M next;

    private DoubleBufferMesh(@NonNull M first, @NonNull M second, @Nullable Swapper<? super M> swapper) {
        this.first = first;
        this.second = second;
        this.swapper = swapper;
    }

    @NonNull
    public static <M extends Mesh> DoubleBufferMesh<M> wrap(@NonNull M mesh, @Nullable Swapper<? super M> swapper) {
        return new DoubleBufferMesh<M>(mesh, (M) mesh.copy(), swapper);
    }

    @Override
    public boolean init() {
        final M next = getNext();
        final boolean initialized = next.init();
        if (initialized) {
            Log.d(TAG, "Initializing next=" + next);
        }
        return initialized;
    }

    @Override
    public boolean initGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        final M next = getNext();
        final boolean initGl = next.initGl(gl, config);
        if (initGl) {
            swap(next);
            return true;
        }

        // initGl must be called for current mesh also as GL instance might have changed
        return getOther(next).initGl(gl, config);
    }

    private void swap(@NonNull M next) {
        synchronized (lock) {
            Log.d(TAG, "Swapping current=" + getMeshName(this.current) + " with next=" + getMeshName(next));
            if (this.current == null) {
                this.next = this.second;
            } else {
                this.next = this.current;
            }
            this.current = next;
            if (swapper != null) {
                swapper.swap(current, this.next);
            }
        }
    }

    @NonNull
    private String getMeshName(@NonNull M mesh) {
        return mesh + "(" + (mesh == this.first ? 0 : 1) + ")";
    }

    @NonNull
    public M getNext() {
        M next;
        synchronized (lock) {
            next = this.next != null ? this.next : this.first;
        }
        return next;
    }

    @NonNull
    public M getFirst() {
        return first;
    }

    @NonNull
    public M getSecond() {
        return second;
    }

    @NonNull
    public M getOther(@NonNull M mesh) {
        return this.first == mesh ? this.second : this.first;
    }

    @Override
    public void draw(@NonNull GL11 gl) {
        final M current;
        synchronized (lock) {
            current = this.current;
        }

        if (current != null) {
            current.draw(gl);
        }
    }

    @NonNull
    @Override
    public M copy() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public State getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlpha(float alpha) {
        this.first.setAlpha(alpha);
        this.second.setAlpha(alpha);
    }

    @Override
    public boolean setColor(@NonNull Color color) {
        final boolean f = this.first.setColor(color);
        final boolean s = this.second.setColor(color);
        return f || s;
    }

    @NonNull
    @Override
    public Color getColor() {
        return this.first.getColor();
    }

    @Override
    public boolean setWidth(int width) {
        final boolean f = this.first.setWidth(width);
        final boolean s = this.second.setWidth(width);
        return f || s;
    }

    @Override
    public int getWidth() {
        return this.first.getWidth();
    }

    public static interface Swapper<M> {
        void swap(@NonNull M current, @NonNull M next);
    }
}

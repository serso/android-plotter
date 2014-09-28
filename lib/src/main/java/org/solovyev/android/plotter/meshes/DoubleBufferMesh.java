package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;

@ThreadSafe
public class DoubleBufferMesh<M extends Mesh> implements Mesh {

	@Nonnull
	private final Object lock = new Object();

	@GuardedBy("lock")
	private M current;

	@GuardedBy("lock")
	private M next;

	@Nonnull
	private final M first;

	@Nonnull
	private final M second;

	private DoubleBufferMesh(@Nonnull M first, @Nonnull M second) {
		this.first = first;
		this.second = second;
	}

	@Nonnull
	public static <M extends Mesh> DoubleBufferMesh<M> wrap(@Nonnull M mesh) {
		return new DoubleBufferMesh<M>(mesh, (M) mesh.copy());
	}

	@Override
	public boolean init() {
		final M next = getNext();
		return next.init();
	}

	@Override
	public boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		final M next = getNext();
		final boolean initGl = next.initGl(gl, config);
		if (initGl) {
			swap(next);
			return false;
		}

		// initGl must be called for current mesh also as GL instance might have changed
		return getOther(next).initGl(gl, config);
	}

	private void swap(@Nonnull M next) {
		synchronized (lock) {
			if (this.current == null) {
				this.next = this.second;
			} else {
				this.next = this.current;
			}
			this.current = next;
		}
	}

	@Nonnull
	public M getNext() {
		M next;
		synchronized (lock) {
			next = this.next != null ? this.next : this.first;
		}
		return next;
	}

	@Nonnull
	public M getOther(@Nonnull M mesh) {
		return this.first == mesh ? this.second : this.first;
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
		final M current;
		synchronized (lock) {
			current = this.current;
		}

		if (current != null) {
			current.draw(gl);
		}
	}

	@Nonnull
	@Override
	public M copy() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public State getState() {
		throw new UnsupportedOperationException();
	}
}

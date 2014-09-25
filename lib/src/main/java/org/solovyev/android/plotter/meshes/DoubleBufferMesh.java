package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;

@ThreadSafe
public class DoubleBufferMesh implements Mesh {

	@Nonnull
	private final Object lock = new Object();

	@GuardedBy("lock")
	private Mesh current;

	@GuardedBy("lock")
	private Mesh next;

	@Nonnull
	private final Mesh first;

	@Nonnull
	private final Mesh second;

	private DoubleBufferMesh(@Nonnull Mesh first, @Nonnull Mesh second) {
		this.first = first;
		this.second = second;
	}

	@Nonnull
	public static DoubleBufferMesh wrap(@Nonnull Mesh mesh) {
		return new DoubleBufferMesh(mesh, mesh.copy());
	}

	@Override
	public void initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		final Mesh next;
		synchronized (lock) {
			next = this.next != null ? this.next : this.first;
		}

		next.initGl(gl, config);

		synchronized (lock) {
			if (this.current == null) {
				this.next = this.second;
			} else {
				this.next = this.current;
			}
			this.current = next;
		}
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
		final Mesh current;
		synchronized (lock) {
			current = this.current;
		}

		if (current != null) {
			current.draw(gl);
		}
	}

	@Nonnull
	@Override
	public Mesh copy() {
		throw new UnsupportedOperationException();
	}
}

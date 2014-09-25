package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.Mesh;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;

@ThreadSafe
public final class Initializer implements Runnable {

	@Nonnull
	private final Iterable<Mesh> meshes;

	@Nonnull
	private final Object lock = new Object();

	@GuardedBy("lock")
	@Nullable
	private GL11 gl;

	@GuardedBy("lock")
	@Nullable
	private MeshConfig config;

	public Initializer(@Nonnull Iterable<Mesh> meshes) {
		this.meshes = meshes;
	}

	public void init(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		synchronized (lock) {
			this.gl = gl;
			this.config = config;
		}
	}

	@Override
	public void run() {
		final GL11 gl;
		final MeshConfig config;
		synchronized (lock) {
			gl = this.gl;
			config = this.config;
		}
		if (gl == null || config == null) {
			throw new IllegalStateException("Initializer must be initialized");
		}

		for (Mesh mesh : meshes) {
			mesh.initGl(gl, config);
		}
	}

	public void cancel() {
	}
}

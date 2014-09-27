package org.solovyev.android.plotter;

import org.solovyev.android.plotter.meshes.Group;
import org.solovyev.android.plotter.meshes.Mesh;

import javax.annotation.Nonnull;

@Nonnull
public final class Initializer implements Runnable {

	@Nonnull
	private final Group<Mesh> group;

	public Initializer(@Nonnull Group<Mesh> group) {
		this.group = group;
	}

	@Override
	public void run() {
		group.init();
	}
}

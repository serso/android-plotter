package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class MeshConfig {

	public boolean useVbo = true;
	public boolean cullFace = false;

	private MeshConfig() {
	}

	@Nonnull
	public static MeshConfig create() {
		return new MeshConfig();
	}

	@Nonnull
	public MeshConfig copy() {
		final MeshConfig copy = new MeshConfig();
		copy.useVbo = useVbo;
		copy.cullFace = cullFace;
		return copy;
	}
}

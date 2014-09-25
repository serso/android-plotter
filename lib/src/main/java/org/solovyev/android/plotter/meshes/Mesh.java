package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public interface Mesh {
	void init();

	void initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config);

	void draw(@Nonnull GL11 gl);

	@Nonnull
	Mesh copy();

	@Nonnull
	State getState();

	enum State {
		DIRTY,
		INIT,
		INIT_GL
	}
}

package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public interface Mesh {
	void init(@Nonnull GL11 gl, @Nonnull MeshConfig config);
	void draw(@Nonnull GL11 gl);
}

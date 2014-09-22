package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public interface Mesh {
	void init(@Nonnull GL11 gl);
	void draw(@Nonnull GL11 gl);
}

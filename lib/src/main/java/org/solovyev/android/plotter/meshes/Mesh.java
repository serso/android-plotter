package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;

public interface Mesh {
	void draw(@Nonnull GL10 gl);
}

package org.solovyev.android.plotter.meshes;

import javax.microedition.khronos.opengles.GL10;

enum IndicesOrder {
	TRIANGLES(GL10.GL_TRIANGLES),
	TRIANGLE_STRIP(GL10.GL_TRIANGLE_STRIP),
	LINE_STRIP(GL10.GL_LINE_STRIP),
	LINES(GL10.GL_LINES),
	;

	public final int glMode;

	IndicesOrder(int glMode) {
		this.glMode = glMode;
	}
}

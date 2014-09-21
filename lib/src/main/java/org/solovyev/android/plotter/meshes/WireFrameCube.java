package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class WireFrameCube extends BaseCube {

	public WireFrameCube(@Nonnull GL11 gl, float width, float height, float depth) {
		super(gl, width, height, depth);

		final short indices[] = {
				// first facet
				0, 1,
				1, 2,
				2, 3,
				3, 0,
				// second facet
				4, 5,
				5, 6,
				6, 7,
				7, 4,
				// connecting edges
				0, 4,
				1, 5,
				2, 6,
				3, 7};

		setIndices(indices, IndicesOrder.LINES);
	}
}

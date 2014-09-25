package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class WireFrameCube extends BaseCube {

	public WireFrameCube(float width, float height, float depth) {
		super(width, height, depth);
	}

	@Override
	public void initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.initGl(gl, config);

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

	@Nonnull
	@Override
	public Mesh copy() {
		return new WireFrameCube(width, height, depth);
	}
}

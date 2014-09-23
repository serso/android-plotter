package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class SolidCube extends BaseCube {

	public SolidCube(float width, float height, float depth) {
		super(width, height, depth);
	}

	@Override
	public void init(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.init(gl, config);

		final short indices[] = {
				0, 4, 5,
				0, 5, 1,
				1, 5, 6,
				1, 6, 2,
				2, 6, 7,
				2, 7, 3,
				3, 7, 4,
				3, 4, 0,
				4, 7, 6,
				4, 6, 5,
				3, 0, 1,
				3, 1, 2};

		setIndices(indices, IndicesOrder.TRIANGLES);
	}
}

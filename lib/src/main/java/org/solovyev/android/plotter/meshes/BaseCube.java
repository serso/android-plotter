package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Color;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public abstract class BaseCube extends BaseMesh {

	protected final float width;
	protected final float height;
	protected final float depth;

	public BaseCube(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	@Override
	public void initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.initGl(gl, config);

		final float x = width / 2;
		final float y = height / 2;
		final float z = depth / 2;

		final float vertices[] = {
				-x, -y, -z, // 0
				x, -y, -z, // 1
				x, y, -z, // 2
				-x, y, -z, // 3
				-x, -y, z, // 4
				x, -y, z, // 5
				x, y, z, // 6
				-x, y, z// 7
		};

		setVertices(vertices);
	}

	@Nonnull
	protected static float[] rainbowVertices() {
		final float colors[] = new float[8 * Color.COMPONENTS];
		Color.fillVertex(colors, 0, Color.RED);
		Color.fillVertex(colors, 1, Color.GREEN);
		Color.fillVertex(colors, 2, Color.BLUE);
		Color.fillVertex(colors, 3, Color.BLACK);
		Color.fillVertex(colors, 4, Color.WHITE);
		Color.fillVertex(colors, 5, Color.MAGENTA);
		Color.fillVertex(colors, 6, Color.GRAY);
		Color.fillVertex(colors, 7, Color.CYAN);
		return colors;
	}
}

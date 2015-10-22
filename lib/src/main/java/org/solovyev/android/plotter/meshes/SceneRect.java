package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class SceneRect extends BaseMesh implements DimensionsAware {
	@Nonnull
	private Dimensions dimensions;

	public SceneRect(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;

	}

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		final float x = dimensions.scene.rect.centerX();
		final float y = dimensions.scene.rect.centerY();
		final float halfWidth = dimensions.scene.width() / 2;
		final float halfHeight = dimensions.scene.height() / 2;

		final float vertices[] = {
				-halfWidth - x, -halfHeight - y, 0, // 0
				halfWidth - x, -halfHeight - y, 0, // 1
				halfWidth - x, halfHeight - y, 0, // 2
				-halfWidth - x, halfHeight - y, 0, // 3
		};

		setVertices(vertices);

		final short indices[] = {
				0, 1,
				1, 2,
				2, 3,
				3, 0
		};

		setIndices(indices, IndicesOrder.LINES);
	}


	@Nonnull
	@Override
	protected BaseMesh makeCopy() {
		return new SceneRect(dimensions);
	}

	@Nonnull
	@Override
	public Dimensions getDimensions() {
		return this.dimensions;
	}

	@Override
	public void setDimensions(@Nonnull Dimensions dimensions) {
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
	}
}

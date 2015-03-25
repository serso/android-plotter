package org.solovyev.android.plotter.meshes;


import org.solovyev.android.plotter.Color;

import javax.annotation.Nonnull;

public class MeshSpec {

	public static final int COLOR_MAP = 0;
	public static final int WIDTH_DEFAULT = 1;
	@Nonnull
	public static final Color COLOR_NO = Color.TRANSPARENT;
	@Nonnull
	public static final Color COLOR_DEFAULT = Color.WHITE;

	@Nonnull
	public Color color;
	public int width;

	private MeshSpec(@Nonnull Color color, int width) {
		this.color = color;
		this.width = width;
	}

	@Nonnull
	public static MeshSpec create(@Nonnull Color color, int width) {
		return new MeshSpec(color, width);
	}

	@Nonnull
	public static MeshSpec createDefault() {
		return new MeshSpec(COLOR_NO, 1);
	}

	@Nonnull
	public MeshSpec copy() {
		return this;
	}

	public void applyTo(@Nonnull DimensionsAware mesh) {
		mesh.setColor(color);
		mesh.setWidth(width);
	}
}
package org.solovyev.android.plotter;

import javax.annotation.Nonnull;

public final class Color {
	public static final Color BLACK = new Color(0xFF000000);
	public static final Color DKGRAY = new Color(0xFF444444);
	public static final Color GRAY = new Color(0xFF888888);
	public static final Color LTGRAY = new Color(0xFFCCCCCC);
	public static final Color WHITE = new Color(0xFFFFFFFF);
	public static final Color RED = new Color(0xFFFF0000);
	public static final Color GREEN = new Color(0xFF00FF00);
	public static final Color BLUE = new Color(0xFF0000FF);
	public static final Color YELLOW = new Color(0xFFFFFF00);
	public static final Color CYAN = new Color(0xFF00FFFF);
	public static final Color MAGENTA = new Color(0xFFFF00FF);
	public static final Color TRANSPARENT = new Color(0);
	public static final int COMPONENTS = 4;

	public final float red;
	public final float green;
	public final float blue;
	public final float alpha;

	public Color(float red, float green, float blue, float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public Color(int color) {
		this.red = red(color);
		this.green = green(color);
		this.blue = blue(color);
		this.alpha = alpha(color);
	}

	static float red(int color) {
		return android.graphics.Color.red(color) / 255f;
	}

	static float green(int color) {
		return android.graphics.Color.green(color) / 255f;
	}

	static float blue(int color) {
		return android.graphics.Color.blue(color) / 255f;
	}

	static float alpha(int color) {
		return android.graphics.Color.alpha(color) / 255f;
	}

	public static void fillVertex(@Nonnull float[] colors, int vertex, @Nonnull Color color) {
		fill(colors, COMPONENTS * vertex, color);
	}

	public static void fill(float[] colors, int index, @Nonnull Color color) {
		colors[index] = color.red;
		colors[index + 1] = color.green;
		colors[index + 2] = color.blue;
		colors[index + 3] = color.alpha;
	}

	public static void fillVertex(@Nonnull float[] colors, int vertex, int color) {
		fill(colors, COMPONENTS * vertex, color);
	}

	public static void fill(float[] colors, int index, int color) {
		colors[index] = red(color);
		colors[index + 1] = green(color);
		colors[index + 2] = blue(color);
		colors[index + 3] = alpha(color);
	}

	public static void fillVertex(float[] colors, int vertex, float red, float green, float blue, float alpha) {
		fill(colors, COMPONENTS * vertex, red, green, blue, alpha);
	}

	public static void fill(float[] colors, int index, float red, float green, float blue, float alpha) {
		colors[index] = red;
		colors[index + 1] = green;
		colors[index + 2] = blue;
		colors[index + 3] = alpha;
	}

	@Nonnull
	public Color transparentCopy(float alpha) {
		return new Color(red, green, blue, alpha);
	}
}

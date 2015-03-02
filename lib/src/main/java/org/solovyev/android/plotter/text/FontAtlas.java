package org.solovyev.android.plotter.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.opengl.GLUtils;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.meshes.IndicesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;

public class FontAtlas {

	private final static char CHAR_FIRST = 32; //" "
	private final static char CHAR_LAST = 126; //"~"
	private final static char CHAR_NONE = 32;
	private static final int CHARS_COUNT = (((CHAR_LAST - CHAR_FIRST) + 1) + 1);
	private final static int CHAR_NONE_POSITION = CHARS_COUNT - 1;

	private final static int FONT_SIZE_MIN = 6;
	private final static int FONT_SIZE_MAX = 512;
	private final static boolean DEBUG = false;

	@Nonnull
	private final AssetManager assets;

	@Nonnull
	private final Font font = new Font();

	@Nonnull
	private final RectF[] chars = new RectF[CHARS_COUNT];

	private int textureId = -1;
	private int cellWidth = 0;
	private int cellHeight = 0;
	private int rows = 0;
	private int cols = 0;
	private int textureSize;

	public FontAtlas(@Nonnull Context context) {
		assets = context.getAssets();
	}

	public boolean init(@Nonnull GL10 gl, @Nonnull String file, int size, int paddingX, int paddingY) {
		Check.isGlThread();

		font.padding.x = paddingX;
		font.padding.y = paddingY;

		final Typeface tf = Typeface.createFromAsset(assets, file);
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(size);
		paint.setColor(0xffffffff);
		paint.setTypeface(tf);

		font.reset(paint);

		cellWidth = (int) font.charWidth + (2 * font.padding.x);
		cellHeight = (int) font.height + (2 * font.padding.y);
		final float cellSize = Math.max(cellHeight, cellWidth);
		if (cellSize < FONT_SIZE_MIN || cellSize > FONT_SIZE_MAX) {
			return false;
		}

		if (cellSize <= 24) {
			textureSize = 256;
		} else if (cellSize <= 40) {
			textureSize = 512;
		} else if (cellSize <= 80) {
			textureSize = 1024;
		} else {
			textureSize = 2048;
		}

		final Bitmap bitmap = drawAtlas(paint);
		bindTexture(gl, bitmap);
		bitmap.recycle();

		resetChars();

		return true;
	}

	private void resetChars() {
		final float scaledCellWidth = cellWidth / (float) textureSize;
		final float scaledCellHeight = cellHeight / (float) textureSize;
		float x = 0;
		float y = 0;
		for (int i = 0; i < CHARS_COUNT; i++) {
			final float left = x / textureSize;
			final float top = y / textureSize;
			final float right = left + scaledCellWidth;
			final float bottom = top + scaledCellHeight;
			chars[i] = new RectF(left, top, right, bottom);
			x += cellWidth;
			if (x + cellWidth >= textureSize) {
				// new row
				x = 0;
				y += cellHeight;
			}
		}
	}

	private void bindTexture(@Nonnull GL10 gl, @Nonnull Bitmap bitmap) {
		final int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		textureId = textures[0];

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	}

	@Nonnull
	private Bitmap drawAtlas(@Nonnull Paint paint) {
		final Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize, Bitmap.Config.ALPHA_8);
		final Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(Color.TRANSPARENT);

		cols = textureSize / cellWidth;
		rows = (int) Math.ceil((float) CHARS_COUNT / (float) cols);

		float x = font.padding.x;
		float y = cellHeight - 1 - font.descent + font.padding.y;

		final char[] in = new char[1];
		for (char i = 0; i < CHARS_COUNT; i++) {
			in[0] = positionToChar(i);
			canvas.drawText(in, 0, 1, x, y, paint);
			if (DEBUG) {
				final float left = x - font.padding.x + 1;
				final float top = y + 1 + font.descent - font.padding.y + 1 - cellHeight;
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawRect(left, top, left + cellWidth - 2, top + cellHeight - 2, paint);
				paint.setStyle(Paint.Style.FILL);
			}
			x += cellWidth;
			final float nextCellXStart = x + cellWidth - font.padding.x;
			if (nextCellXStart > textureSize) {
				// new row
				x = font.padding.x;
				y += cellHeight;
			}
		}
		return bitmap;
	}

	@Nonnull
	public MeshData getMeshData(float x, float y, float z, int size) {
		return MeshData.createForFullAtlas(this, x, y, z, size);
	}

	@Nonnull
	public MeshData getMeshData(@Nonnull final String s, float x, float y, float z, float scale) {
		return getMeshData(s, x, y, z, scale, false, false);
	}

	@Nonnull
	public MeshData getMeshData(@Nonnull final String s, float x, float y, float z, float scale, boolean centerX, boolean centerY) {
		final List<MeshData> meshDataList = new ArrayList<>(s.length());

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			meshDataList.add(MeshData.createForChar(this, c, x, y, z, cellWidth * scale, cellHeight * scale));
			x += font.charWidths[charToPosition(c)] * scale;
		}
		return new MeshData(meshDataList, centerX, centerY);
	}

	@Nonnull
	private float[] getTextureCoordinates(char c) {
		final RectF bounds = chars[charToPosition(c)];
		return new float[]{
				bounds.left, bounds.bottom,
				bounds.right, bounds.bottom,
				bounds.left, bounds.top,
				bounds.right, bounds.top,
		};
	}
	
	public float getFontHeight() {
		return font.height;
	}

	private final static class Font {
		final Point padding = new Point();

		float height = 0.0f;
		float ascent = 0.0f;
		float descent = 0.0f;

		float charWidth = 0;
		float charHeight = 0;

		final float[] charWidths = new float[CHARS_COUNT];

		public void reset(@Nonnull Paint paint) {
			final Paint.FontMetrics fm = paint.getFontMetrics();

			height = (float) Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top));
			ascent = (float) Math.ceil(Math.abs(fm.ascent));
			descent = (float) Math.ceil(Math.abs(fm.descent));

			charWidth = 0;
			charHeight = 0;

			final char[] in = new char[1];
			final float[] out = new float[1];
			for (int i = 0; i < CHARS_COUNT; i++) {
				in[0] = positionToChar(i);
				paint.getTextWidths(in, 0, 1, out);
				charWidths[i] = out[0];
				charWidth = Math.max(charWidth, charWidths[i]);
			}

			charHeight = height;
		}

		public float getRatio(char c) {
			final int position = charToPosition(c);
			return charWidths[position] / charHeight;
		}
	}

	private static char positionToChar(int position) {
		Check.isTrue(position >= 0 && position < CHARS_COUNT, "Out of bounds");
		if (position == CHAR_NONE_POSITION) {
			return CHAR_NONE;
		} else {
			return (char) (position + CHAR_FIRST);
		}
	}

	private static int charToPosition(char c) {
		if (c >= CHAR_FIRST && c <= CHAR_LAST) {
			return c - CHAR_FIRST;
		} else {
			return CHAR_NONE_POSITION;
		}
	}

	public static final class MeshData {
		@Nonnull
		public final short[] indices;
		@Nonnull
		public final IndicesOrder indicesOrder;
		@Nonnull
		public final float[] vertices;
		public final int textureId;
		@Nonnull
		public final float[] textureCoordinates;

		private MeshData(int textureId, @Nonnull short[] indices, @Nonnull IndicesOrder indicesOrder, @Nonnull float[] vertices, @Nonnull float[] textureCoordinates) {
			this.textureId = textureId;
			this.indices = indices;
			this.indicesOrder = indicesOrder;
			this.vertices = vertices;
			this.textureCoordinates = textureCoordinates;
		}

		public MeshData(@Nonnull List<MeshData> meshDataList, boolean centerX, boolean centerY) {
			indicesOrder = meshDataList.get(0).indicesOrder;
			textureId = meshDataList.get(0).textureId;

			int indicesCount = 0;
			int verticesCount = 0;
			int textureCoordinatesCount = 0;
			float minX = Integer.MAX_VALUE;
			float maxX = Integer.MIN_VALUE;
			float minY = Integer.MAX_VALUE;
			float maxY = Integer.MIN_VALUE;
			for (MeshData meshData : meshDataList) {
				Check.isTrue(indicesOrder == meshData.indicesOrder, "Must be equal");
				Check.isTrue(textureId == meshData.textureId, "Must be equal");
				indicesCount += meshData.indices.length;
				verticesCount += meshData.vertices.length;
				textureCoordinatesCount += meshData.textureCoordinates.length;
				if (centerX || centerY) {
					for (int i = 0; i < meshData.vertices.length; i += 3) {
						final float x = meshData.vertices[i];
						minX = Math.min(minX, x);
						maxX = Math.max(maxX, x);
						final float y = meshData.vertices[i + 1];
						minY = Math.min(minY, y);
						maxY = Math.max(maxY, y);
					}
				}
			}
			indices = new short[indicesCount];
			vertices = new float[verticesCount];
			textureCoordinates = new float[textureCoordinatesCount];

			indicesCount = 0;
			verticesCount = 0;
			textureCoordinatesCount = 0;
			for (MeshData meshData : meshDataList) {
				for (int i = 0; i < meshData.indices.length; i++) {
					indices[indicesCount + i] = (short) (meshData.indices[i] + verticesCount / 3);
				}
				indicesCount += meshData.indices.length;

				if (centerX || centerY) {
					final float dx = centerX ? Math.abs(maxX - minX) / 2 : 0f;
					final float dy = centerY ? Math.abs(maxY - minY) / 2 : 0f;
					for (int i = 0; i < meshData.vertices.length; i += 3) {
						vertices[verticesCount + i] = meshData.vertices[i] - dx;
						vertices[verticesCount + i + 1] = meshData.vertices[i + 1] - dy;
						vertices[verticesCount + i + 2] = meshData.vertices[i + 2];
					}
				} else {
					System.arraycopy(meshData.vertices, 0, vertices, verticesCount, meshData.vertices.length);
				}
				verticesCount += meshData.vertices.length;

				System.arraycopy(meshData.textureCoordinates, 0, textureCoordinates, textureCoordinatesCount, meshData.textureCoordinates.length);
				textureCoordinatesCount += meshData.textureCoordinates.length;
			}
		}

		@Nonnull
		static MeshData createForFullAtlas(@Nonnull FontAtlas atlas, float x, float y, float z, int size) {
			final short[] indices = new short[]{
					0, 1, 2,
					1, 3, 2};
			final float[] vertices = new float[]{
					x, y, z,
					x + size, y, z,
					x, y + size, z,
					x + size, y + size, z};
			final float[] textureCoordinates = {
					0.0f, 1.0f,
					1.0f, 1.0f,
					0.0f, 0.0f,
					1.0f, 0.0f};
			return new MeshData(atlas.textureId, indices, IndicesOrder.TRIANGLES, vertices, textureCoordinates);
		}

		@Nonnull
		static MeshData createForChar(@Nonnull FontAtlas atlas, char c, float x, float y, float z, float width, float height) {
			final short[] indices = new short[]{
					0, 1, 2,
					1, 3, 2};
			final float[] vertices = new float[]{
					x, y, z,
					x + width, y, z,
					x, y + height, z,
					x + width, y + height, z};
			return new MeshData(atlas.textureId, indices, IndicesOrder.TRIANGLES, vertices, atlas.getTextureCoordinates(c));
		}
	}
}

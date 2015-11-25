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
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.arrays.FloatArray;

import java.util.List;

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

    @NonNull
    private final AssetManager assets;

    @NonNull
    private final Font font = new Font();

    @NonNull
    private final TextMeshesPool meshesPool = new TextMeshesPool();

    @NonNull
    private final ListsPool listsPool = new ListsPool();

    @NonNull
    private final RectF[] chars = new RectF[CHARS_COUNT];

    private int textureId = -1;
    private int cellWidth = 0;
    private int cellHeight = 0;
    private int rows = 0;
    private int cols = 0;
    private int textureSize;

    public FontAtlas(@NonNull Context context) {
        assets = context.getAssets();
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

    public int getTextureId() {
        return textureId;
    }

    @NonNull
    public TextMesh mergeMeshes(@NonNull List<TextMesh> meshes, int meshSize, boolean centerX, boolean centerY) {
        final TextMesh result = meshesPool.obtain(meshSize);
        result.merge(meshes, centerX, centerY);
        return result;
    }

    public boolean init(@NonNull GL10 gl, @NonNull String file, int size, int paddingX, int paddingY, int color) {
        Check.isGlThread();

        font.padding.x = paddingX;
        font.padding.y = paddingY;

        final Typeface tf = Typeface.createFromAsset(assets, file);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(size);
        paint.setColor(color);
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

    private void bindTexture(@NonNull GL10 gl, @NonNull Bitmap bitmap) {
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

    @NonNull
    private Bitmap drawAtlas(@NonNull Paint paint) {
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

    @NonNull
    public TextMesh getMesh(float x, float y, float z, int size) {
        return TextMesh.createForFullAtlas(this, x, y, z, size);
    }

    @NonNull
    public TextMesh getMesh(@NonNull final String s, float x, float y, float z, float scale) {
        return getMesh(s, x, y, z, scale, false, false);
    }

    @NonNull
    public TextMesh getMesh(@NonNull final String s, float x, float y, float z, float scale, boolean centerX, boolean centerY) {
        final List<TextMesh> meshes = listsPool.obtain();

        int meshSize = 0;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final TextMesh mesh = TextMesh.createForChar(this, c, x, y, z, cellWidth * scale, cellHeight * scale);
            meshSize += mesh.size;
            meshes.add(mesh);
            x += font.charWidths[charToPosition(c)] * scale;
        }
        final TextMesh result = mergeMeshes(meshes, meshSize, centerX, centerY);
        releaseMeshes(meshes);
        return result;
    }

    void setTextureCoordinates(@NonNull FloatArray textureCoordinates) {
        textureCoordinates.array[0] = 0;
        textureCoordinates.array[1] = 1;
        textureCoordinates.array[2] = 1;
        textureCoordinates.array[3] = 1;
        textureCoordinates.array[4] = 0;
        textureCoordinates.array[5] = 0;
        textureCoordinates.array[6] = 1;
        textureCoordinates.array[7] = 0;
        textureCoordinates.size = 8;
    }

    void setTextureCoordinates(char c, @NonNull FloatArray textureCoordinates) {
        final RectF bounds = chars[charToPosition(c)];
        textureCoordinates.array[0] = bounds.left;
        textureCoordinates.array[1] = bounds.bottom;
        textureCoordinates.array[2] = bounds.right;
        textureCoordinates.array[3] = bounds.bottom;
        textureCoordinates.array[4] = bounds.left;
        textureCoordinates.array[5] = bounds.top;
        textureCoordinates.array[6] = bounds.right;
        textureCoordinates.array[7] = bounds.top;
        textureCoordinates.size = 8;
    }

    public float getFontHeight() {
        return font.height;
    }

    private void releaseMeshes(@NonNull List<TextMesh> meshes) {
        for (int i = meshes.size() - 1; i >= 0; i--) {
            releaseMesh(meshes.remove(i));
        }
        listsPool.release(meshes);
    }

    public void releaseMesh(@NonNull TextMesh mesh) {
        meshesPool.release(mesh);
    }

    @NonNull
    TextMesh obtainMesh(int meshSize) {
        return meshesPool.obtain(meshSize);
    }

    private final static class Font {
        final Point padding = new Point();
        final float[] charWidths = new float[CHARS_COUNT];
        float height = 0.0f;
        float ascent = 0.0f;
        float descent = 0.0f;
        float charWidth = 0;
        float charHeight = 0;

        public void reset(@NonNull Paint paint) {
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
}

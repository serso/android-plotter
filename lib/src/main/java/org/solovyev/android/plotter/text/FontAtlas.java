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
import android.support.annotation.Nullable;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.meshes.IndicesOrder;

import java.util.ArrayList;
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
    private final Pool pool = new Pool();

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
    public Mesh mergeMeshes(@NonNull List<Mesh> meshes, int meshSize, boolean centerX, boolean centerY) {
        final Mesh result = pool.obtain(meshSize);
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
    public Mesh getMesh(float x, float y, float z, int size) {
        return Mesh.createForFullAtlas(this, x, y, z, size);
    }

    @NonNull
    public Mesh getMesh(@NonNull final String s, float x, float y, float z, float scale) {
        return getMesh(s, x, y, z, scale, false, false);
    }

    @NonNull
    public Mesh getMesh(@NonNull final String s, float x, float y, float z, float scale, boolean centerX, boolean centerY) {
        final List<Mesh> meshes = new ArrayList<>(s.length());

        int meshSize = 0;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final Mesh mesh = Mesh.createForChar(this, c, x, y, z, cellWidth * scale, cellHeight * scale);
            meshSize += mesh.size;
            meshes.add(mesh);
            x += font.charWidths[charToPosition(c)] * scale;
        }
        final Mesh result = mergeMeshes(meshes, meshSize, centerX, centerY);
        releaseMeshes(meshes);
        return result;
    }

    private void setTextureCoordinates(char c, float[] textureCoordinates) {
        final RectF bounds = chars[charToPosition(c)];
        textureCoordinates[0] = bounds.left;
        textureCoordinates[1] = bounds.bottom;
        textureCoordinates[2] = bounds.right;
        textureCoordinates[3] = bounds.bottom;
        textureCoordinates[4] = bounds.left;
        textureCoordinates[5] = bounds.top;
        textureCoordinates[6] = bounds.right;
        textureCoordinates[7] = bounds.top;
    }

    public float getFontHeight() {
        return font.height;
    }

    public void releaseMeshes(@NonNull List<Mesh> meshList) {
        for (int i = 0; i < meshList.size(); i++) {
            releaseMesh(meshList.get(i));
        }
    }

    public void releaseMesh(@NonNull Mesh mesh) {
        pool.release(mesh);
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

    public static final class Mesh {
        public int size;
        public short[] indices;
        public int indicesCount;
        public final IndicesOrder indicesOrder;
        public float[] vertices;
        public int verticesCount;
        public float[] textureCoordinates;
        public int textureCoordinatesCount;
        @Nullable
        private RectF bounds;

        public Mesh(int size) {
            Check.isTrue(size > 0);
            this.indicesOrder = IndicesOrder.TRIANGLES;
            setSize(size);
        }

        private void setSize(int size) {
            this.size = size;
            final short[] newIndices = new short[size * 6];
            if (indices != null) {
                System.arraycopy(indices, 0, newIndices, 0, indices.length);
            }
            indices = newIndices;
            final float[] newVertices = new float[size * 4 * 3];
            if (vertices != null) {
                System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            }
            vertices = newVertices;
            final float[] newTextureCoordinates = new float[size * 4 * 2];
            if (textureCoordinates != null) {
                System.arraycopy(textureCoordinates, 0, newTextureCoordinates, 0, textureCoordinates.length);
            }
            textureCoordinates = newTextureCoordinates;
        }

        @NonNull
        static Mesh createForFullAtlas(@NonNull FontAtlas atlas, float x, float y, float z, int size) {
            final Mesh mesh = atlas.pool.obtain(1);
            mesh.fill(x, y, z, size, size);
            mesh.textureCoordinates[0] = 0;
            mesh.textureCoordinates[1] = 1;
            mesh.textureCoordinates[2] = 1;
            mesh.textureCoordinates[3] = 1;
            mesh.textureCoordinates[4] = 0;
            mesh.textureCoordinates[5] = 0;
            mesh.textureCoordinates[6] = 1;
            mesh.textureCoordinates[7] = 0;
            return mesh;
        }

        @NonNull
        static Mesh createForChar(@NonNull FontAtlas atlas, char c, float x, float y, float z, float width, float height) {
            final Mesh mesh = atlas.pool.obtain(1);
            mesh.fill(x, y, z, width, height);
            atlas.setTextureCoordinates(c, mesh.textureCoordinates);
            return mesh;
        }

        private void fill(float x, float y, float z, float width, float height) {
            Check.isTrue(size == 1);
            indices[0] = 0;
            indices[1] = 1;
            indices[2] = 2;
            indices[3] = 1;
            indices[4] = 3;
            indices[5] = 2;
            vertices[0] = x;
            vertices[1] = y;
            vertices[2] = z;
            vertices[3] = x + width;
            vertices[4] = y;
            vertices[5] = z;
            vertices[6] = x;
            vertices[7] = y + height;
            vertices[8] = z;
            vertices[9] = x + width;
            vertices[10] = y + height;
            vertices[11] = z;
            indicesCount = indices.length;
            verticesCount = vertices.length;
            textureCoordinatesCount = textureCoordinates.length;
        }

        @NonNull
        public RectF getBounds() {
            initBounds();
            return union(bounds);
        }

        private void initBounds() {
            if (bounds == null) {
                bounds = new RectF();
            }
            bounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        }

        @NonNull
        private RectF union(@NonNull RectF bounds) {
            for (int i = 0; i < vertices.length; i += 3) {
                final float x = vertices[i];
                bounds.left = Math.min(bounds.left, x);
                bounds.right = Math.max(bounds.right, x);
                final float y = vertices[i + 1];
                bounds.top = Math.min(bounds.top, y);
                bounds.bottom = Math.max(bounds.bottom, y);
            }
            return bounds;
        }

        public void translate(float dx, float dy) {
            if (dx == 0 && dy == 0) {
                return;
            }
            for (int i = 0; i < vertices.length; i += 3) {
                vertices[i] += dx;
                vertices[i + 1] += dy;
            }
            getBounds().offset(dx, dy);
        }

        public void merge(@NonNull List<Mesh> meshes, boolean centerX, boolean centerY) {
            initBounds();
            for (int i = 0; i < meshes.size(); i++) {
                final Mesh mesh = meshes.get(i);
                if (centerX || centerY) {
                    mesh.union(bounds);
                }
            }
            for (int i = 0; i < meshes.size(); i++) {
                merge(meshes.get(i), centerX, centerY);
            }
        }

        public void merge(@NonNull Mesh mesh) {
            merge(mesh, false, false);
        }

        private void merge(@NonNull Mesh mesh, boolean centerX, boolean centerY) {
            if (indices.length < indicesCount + mesh.indicesCount) {
                final int missing = (indicesCount + mesh.indicesCount - indices.length) / 6 + 1;
                setSize((int) Math.max(1.5 * size, size + missing));
            }
            for (int i = 0; i < mesh.indicesCount; i++) {
                indices[indicesCount + i] = (short) (mesh.indices[i] + verticesCount / 3);
            }
            indicesCount += mesh.indicesCount;

            if (centerX || centerY) {
                final float dx = centerX ? Math.abs(bounds.right - bounds.left) / 2 : 0f;
                final float dy = centerY ? Math.abs(bounds.top - bounds.bottom) / 2 : 0f;
                for (int i = 0; i < mesh.verticesCount; i += 3) {
                    vertices[verticesCount + i] = mesh.vertices[i] - dx;
                    vertices[verticesCount + i + 1] = mesh.vertices[i + 1] - dy;
                    vertices[verticesCount + i + 2] = mesh.vertices[i + 2];
                }
            } else {
                System.arraycopy(mesh.vertices, 0, vertices, verticesCount, mesh.verticesCount);
            }
            verticesCount += mesh.verticesCount;

            System.arraycopy(mesh.textureCoordinates, 0, textureCoordinates, textureCoordinatesCount, mesh.textureCoordinatesCount);
            textureCoordinatesCount += mesh.textureCoordinatesCount;
        }

        public void reset() {
            verticesCount = 0;
            indicesCount = 0;
            textureCoordinatesCount = 0;
        }
    }

    private final class Pool {

        private static final int MAX_MESH_SIZE = 10;
        @NonNull
        private List<List<Mesh>> pool = new ArrayList<>();

        {
            for (int i = 0; i < MAX_MESH_SIZE; i++) {
                pool.add(new ArrayList<Mesh>());
            }
        }

        @NonNull
        public Mesh obtain(int meshSize) {
            final Mesh mesh;
            if (meshSize >= MAX_MESH_SIZE) {
                return new Mesh(meshSize);
            }
            final List<Mesh> list = pool.get(meshSize);
            final int size = list.size();
            if (size == 0) {
                mesh = new Mesh(meshSize);
            } else {
                mesh = list.remove(size - 1);
            }
            return mesh;
        }

        public void release(@NonNull Mesh mesh) {
            if (mesh.size >= MAX_MESH_SIZE) {
                return;
            }
            final List<Mesh> list = pool.get(mesh.size);
            list.add(mesh);
            mesh.reset();
        }
    }
}

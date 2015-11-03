package org.solovyev.android.plotter.meshes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.MeshConfig;

import javax.microedition.khronos.opengles.GL11;

public class DrawableTexture extends BaseMesh {
    @NonNull
    private final Resources resources;
    @DrawableRes
    private final int drawable;

    public DrawableTexture(@NonNull Resources resources, @DrawableRes int drawable) {
        this.resources = resources;
        this.drawable = drawable;
    }

    @Override
    protected void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        final short[] indices = new short[]{
                0, 1, 2,
                1, 3, 2};

        final float[] vertices = new float[]{
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.0f};

        setIndices(indices, IndicesOrder.TRIANGLES);
        setVertices(vertices);
        final Bitmap bitmap = BitmapFactory.decodeResource(resources, drawable);
        loadTexture(gl, bitmap);
        bitmap.recycle();
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new DrawableTexture(resources, drawable);
    }
}

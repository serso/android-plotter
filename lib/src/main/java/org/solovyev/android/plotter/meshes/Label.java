package org.solovyev.android.plotter.meshes;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.MeshConfig;
import org.solovyev.android.plotter.text.FontAtlas;

import javax.microedition.khronos.opengles.GL11;

public class Label extends BaseMesh {
    @NonNull
    private final FontAtlas fontAtlas;

    @NonNull
    private final String label;

    public Label(@NonNull FontAtlas fontAtlas, @NonNull String label) {
        this.fontAtlas = fontAtlas;
        this.label = label;
    }

    @Override
    protected void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);

        final FontAtlas.MeshData meshData;
        meshData = fontAtlas.getMeshData(label, -0.5f, 0.0f, 0f, 0.003f);
        setIndices(meshData.indices, meshData.indicesOrder);
        setVertices(meshData.vertices);
        setTexture(meshData.textureId, meshData.textureCoordinates);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new Label(fontAtlas, label);
    }
}

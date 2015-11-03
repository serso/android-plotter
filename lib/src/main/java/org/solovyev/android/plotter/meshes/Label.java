package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;
import org.solovyev.android.plotter.text.FontAtlas;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public class Label extends BaseMesh {
    @Nonnull
    private final FontAtlas fontAtlas;

    @Nonnull
    private final String label;

    public Label(@Nonnull FontAtlas fontAtlas, @Nonnull String label) {
        this.fontAtlas = fontAtlas;
        this.label = label;
    }

    @Override
    protected void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
        super.onInitGl(gl, config);

        final FontAtlas.MeshData meshData;
        meshData = fontAtlas.getMeshData(label, -0.5f, 0.0f, 0f, 0.003f);
        setIndices(meshData.indices, meshData.indicesOrder);
        setVertices(meshData.vertices);
        setTexture(meshData.textureId, meshData.textureCoordinates);
    }

    @Nonnull
    @Override
    protected BaseMesh makeCopy() {
        return new Label(fontAtlas, label);
    }
}

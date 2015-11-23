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

        final FontAtlas.Mesh mesh = fontAtlas.getMesh(label, -0.5f, 0.0f, 0f, 0.003f);
        setIndices(mesh.indices, mesh.indicesOrder);
        setVertices(mesh.vertices);
        setTexture(fontAtlas.getTextureId(), mesh.textureCoordinates);
        fontAtlas.releaseMesh(mesh);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new Label(fontAtlas, label);
    }
}

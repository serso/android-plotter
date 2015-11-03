package org.solovyev.android.plotter;

import android.support.annotation.NonNull;

public final class MeshConfig {

    public boolean useVbo = true;
    public boolean cullFace = false;

    private MeshConfig() {
    }

    @NonNull
    public static MeshConfig create() {
        return new MeshConfig();
    }

    @NonNull
    public MeshConfig copy() {
        final MeshConfig copy = new MeshConfig();
        copy.useVbo = useVbo;
        copy.cullFace = cullFace;
        return copy;
    }
}

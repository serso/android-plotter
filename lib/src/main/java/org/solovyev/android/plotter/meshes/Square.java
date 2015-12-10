package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;

public class Square extends ShapeMesh {
    public Square(@NonNull Dimensions dimensions, @NonNull PointF center) {
        super(dimensions, center);
    }

    @Override
    protected void fillPath(@NonNull ShapePath path, @NonNull Dimensions dimensions) {
        path.append(dimensions, -1, 1);
        path.append(dimensions, 1, 1);
        path.append(dimensions, 1, -1);
        path.append(dimensions, -1, -1);
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new Square(getDimensions(), center);
    }
}

package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;

public class Circle extends ShapeMesh {
    private final float radius;

    public Circle(@NonNull Dimensions dimensions, @NonNull PointF center, float radius) {
        super(dimensions, center);
        this.radius = radius;
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new Circle(dimensions.get(), center, radius);
    }

    @Override
    protected void fillPath(@NonNull ShapePath path, @NonNull Dimensions dimensions) {
        final int points = dimensions.scene.view.width / 10;
        final float maxAngle = (float) (2 * Math.PI);
        for (float angle = 0; angle < maxAngle; angle += maxAngle / points) {
            path.append(dimensions, (float)Math.cos(angle) * radius, (float)Math.sin(angle) * radius);
        }
    }
}

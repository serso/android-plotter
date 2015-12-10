package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;

class Graph extends Path {

    float step = -1f;
    final PointF center = new PointF();

    private Graph() {
    }

    @NonNull
    static Graph create() {
        return new Graph();
    }

    public void moveStartTo(float x) {
        checkIsNotEmpty();
        while (start < end && vertices[start] < x) {
            start += 3;
        }

        if (start > end) {
            start = end;
        }
    }

    private void checkIsNotEmpty() {
        Check.isTrue(!isEmpty(), "Should not be empty");
    }

    public void moveEndTo(float x) {
        checkIsNotEmpty();
        while (start < end && vertices[end - 3] > x) {
            end -= 3;
        }

        if (start > end) {
            end = start;
        }
    }

    public float xMin() {
        return vertices[start];
    }

    public float xMax() {
        return vertices[end - 3];
    }
}

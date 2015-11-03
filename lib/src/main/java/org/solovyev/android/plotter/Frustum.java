package org.solovyev.android.plotter;

import android.support.annotation.NonNull;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public final class Frustum {

    public static final float SCENE_WIDTH = 2f;
    private static final String TAG = Plot.getTag("Frustum");
    /*
    View from top
                                             +
                              +              |
        near   +    k*near    |    k*near    |
    +---------------------------------------------->
               +              |              |
                              +              |
                                             +
                far=near+2*k*near
    +---------------------------------------->
     */
    // constant to calculate distance between "near" plane and scene plane (dist("near", "scene") == dist("scene", "far")),
    // multiplier of "near" (K * near)
    private static final int K = 2;
    // horizontal view angle in radians
    private static final float VIEW_ANGLE = (float) (Math.PI * 30f / 180f);
    private static final float TAN = (float) Math.tan(VIEW_ANGLE / 2f);

    // width and height of the "near" clipping plane
    @NonNull
    private final RectSizeF nearSize = new RectSizeF();
    @NonNull
    private final RectSizeF sceneSize = new RectSizeF();

    private float near;
    private float far;

    private float distance;

    @NonNull
    private Zoom zoom = Zoom.one();
    private float aspectRatio;

    private Frustum(@NonNull Zoom zoom, float aspectRatio) {
        update(zoom, aspectRatio);
    }

    @NonNull
    static Frustum empty() {
        return new Frustum(Zoom.one(), 1);
    }

    boolean update(@NonNull Zoom zoom, float aspectRatio) {
        if (this.aspectRatio == aspectRatio && this.zoom.equals(zoom)) {
            return false;
        }
        this.aspectRatio = aspectRatio;
        this.zoom = zoom;

        recalculate();

        Log.d(TAG, "Frustum: near=" + near + ", far=" + far + ", distance=" + distance);
        Log.d(TAG, "Scene " + sceneSize);

        return true;

    }

    private void recalculate() {
        // we assume that:
        // 1. if zoom == 1 then width of the scene should be 1 and height = 1 * aspectRation
        // 2. far = 3 * near
        // 3. view angle is constant and equals to 60 degrees
        near = 1f / ((K + 1) * TAN);
        far = near + 2 * K * near;

        distance = (K + 1) * near;

        nearSize.width = SCENE_WIDTH / (K + 1);
        nearSize.height = nearSize.width / aspectRatio;

        sceneSize.width = SCENE_WIDTH;
        sceneSize.height = sceneSize.width / aspectRatio;

        multiplyBy(zoom.level);
    }

    private void multiplyBy(float value) {
        near *= value;
        far *= value;
        distance *= value;
        nearSize.multiplyBy(value);
        sceneSize.multiplyBy(value);
    }

    @NonNull
    public RectSizeF getSceneSize() {
        return sceneSize;
    }

    public void updateGl(@NonNull GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        final float halfWidth = nearSize.width / 2;
        final float halfHeight = nearSize.height / 2;
        gl.glFrustumf(-halfWidth, halfWidth, -halfHeight, halfHeight, near, far);
    }

    public float distance() {
        return distance;
    }
}

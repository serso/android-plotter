/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.solovyev.android.plotter;

import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import javaz.annotation.concurrent.GuardedBy;

@SuppressWarnings("SynchronizeOnNonFinalField")
final class PlotRenderer implements GLSurfaceView.Renderer {

    @NonNull
    private static final Object SOURCE = new Object();

    @NonNull
    private static final PointF tmp = new PointF();

    @NonNull
    private final Spf spf = new Spf();

    // lock for synchronization GL objects
    @NonNull
    private final Object lock = new Object();

    @NonNull
    private final PlottingView view;
    @NonNull
    private final RotationHolder rotation = new RotationHolder();
    @NonNull
    private final ZoomerHolder zoomer = new ZoomerHolder();
    @NonNull
    private final FaderHolder fader = new FaderHolder();
    @NonNull
    private final CameraManHolder cameraMan = new CameraManHolder();
    @NonNull
    private final RectSize viewSize = RectSize.empty();
    @NonNull
    private final Frustum frustum = Frustum.empty();
    @GuardedBy("lock")
    @Nullable
    private Plotter plotter;
    @GuardedBy("lock")
    private boolean glInitialized;
    @GuardedBy("lock")
    @NonNull
    private PointF camera = new PointF();
    private volatile boolean rotating = rotation.shouldRotate();

    public PlotRenderer(@NonNull PlottingView view) {
        this.view = view;
    }

    @Nullable
    private Plotter getPlotter() {
        // plotter might changed only from NULL to some object => if change is already visible from GL thread just use
        // the object. Otherwise, do the whole sync block thing.
        Plotter localPlotter = plotter;
        if (localPlotter != null) {
            return localPlotter;
        }
        synchronized (lock) {
            localPlotter = plotter;
        }
        return localPlotter;
    }

    public void setPlotter(@NonNull Plotter plotter) {
        synchronized (lock) {
            Check.isNull(this.plotter);
            this.plotter = plotter;
            if (!viewSize.isEmpty()) {
                this.plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        synchronized (lock) {
            glInitialized = false;
        }
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        // let's use fastest perspective calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glShadeModel(GL10.GL_SMOOTH);

        tryInitGl(gl, false);
    }

    private void tryInitGl(@NonNull GL10 gl, boolean fromFrameDraw) {
        Check.isGlThread();

        // state might be changed only from false to true. If change is already visible from GL thread there is no
        // point in obtaining the lock. If change is not visible from GL thread - let's obtain the lock and check again
        if (glInitialized) {
            return;
        }

        synchronized (lock) {
            if (glInitialized) {
                return;
            }
            if (plotter != null) {
                initGl((GL11) gl, plotter, fromFrameDraw);
            }
        }
    }

    private void initGl(@NonNull GL11 gl, @NonNull Plotter plotter, boolean fromFrameDraw) {
        Check.isTrue(Thread.holdsLock(lock), "Should be called from synchronized block");
        Check.isTrue(!glInitialized, "Should be not initialized");
        Check.isGlThread();

        // surface was created, let's reinitialize all meshes if needed (just to avoid reinitialization in onDrawFrame)
        plotter.initGl(gl, true);

        if (!fromFrameDraw) {
            // we must to request rendering
            view.requestRender();
        }

        glInitialized = true;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        spf.logFrameStart();
        tryInitGl(gl10, true);

        final Plotter plotter = getPlotter();
        if (plotter != null) {
            final GL11 gl = (GL11) gl10;

            final float alpha = fader.onFrame();
            zoomer.onFrame(gl, plotter);

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            cameraMan.onFrame(tmp, plotter);
            //gl.glTranslatef(tmp.x, tmp.y, -frustum.distance());

            rotation.onFrame(gl10);

            plotter.initGl(gl, false);

            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            plotter.draw(gl, alpha);

            gl.glDisable(GL10.GL_BLEND);
        }

        if (rotating) {
            view.requestRender();
        }
        spf.logFrameEnd();
    }

    private void initFrustum(@NonNull GL10 gl, @NonNull Zoom zoom) {
        initFrustum(gl, zoom, false);
    }

    private void initFrustum(@NonNull GL10 gl, @NonNull Zoom zoom, boolean firstTime) {
        Check.isGlThread();

        if (viewSize.isEmpty()) {
            return;
        }

        if (frustum.update(zoom, viewSize.aspectRatio()) || firstTime) {
            frustum.updateGl(gl);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, final int width, final int height) {
        viewSize.set(width, height);
        gl.glViewport(0, 0, width, height);
        zoomer.onSurfaceChanged(gl);
        view.post(new Runnable() {
            @Override
            public void run() {
                final Plotter plotter = getPlotter();
                if (plotter != null) {
                    plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
                }
                view.onSizeChanged(viewSize);
            }
        });
    }

    public void rotate(float dx, float dy) {
        rotation.setRotationSpeed(dx, dy);
    }

    public void rotateTo(float x, float y) {
        rotation.setRotationTo(x, y);
    }

    public void setRotationSpeed(float speedX, float speedY) {
        rotation.setRotationSpeed(speedX, speedY);
    }

    public void stopRotating() {
        rotating = false;
    }

    public void startRotating() {
        synchronized (rotation) {
            final boolean shouldRotate = rotation.shouldRotate();
            if (rotating == shouldRotate) {
                return;
            }
            rotating = shouldRotate;
            if (rotating) {
                view.requestRender();
            }
        }
    }

    public void saveState(@NonNull Bundle bundle) {
        Check.isMainThread();
        rotation.saveState(bundle);
        zoomer.saveState(bundle);
        cameraMan.saveState(bundle);
    }

    public void restoreState(@NonNull Bundle bundle) {
        Check.isMainThread();
        rotation.restoreState(bundle);
        zoomer.restoreState(bundle);
        cameraMan.restoreState(bundle);
        view.requestRender();
    }

    public boolean canZoom(boolean in) {
        return zoomer.canZoom(in);
    }

    public void zoom(boolean in) {
        zoomer.zoom(in);
    }

    public void resetZoom() {
        zoomer.reset();
    }

    public void zoomBy(@NonNull ZoomLevels level) {
        zoomer.zoomBy(level);
    }

    public void setPinchZoom(boolean pinchZoom) {
        zoomer.setPinchZoom(pinchZoom);
    }

    public void moveCamera(float dx, float dy) {
        Check.isMainThread();
        synchronized (lock) {
            camera.offset(dx, -dy);
            final Plotter plotter = getPlotter();
            if (plotter != null) {
                plotter.onCameraMoved(dx, -dy);
            }
        }
        fader.fadeOut();
        view.requestRender();
    }

    public void stopMovingCamera() {
        Check.isMainThread();

        final Plotter plotter = getPlotter();
        if (plotter != null) {
            plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
        }
        fader.fadeIn();
        view.requestRender();
    }

    @NonNull
    private PointF getSceneCenter() {
        synchronized (lock) {
            return getSceneCenter(camera);
        }
    }

    @NonNull
    private PointF getSceneCenter(@NonNull PointF camera) {
        return new PointF(-camera.x, -camera.y);
    }

    @NonNull
    private RectSizeF getSceneSize() {
        final RectSizeF result = new RectSizeF();
        synchronized (lock) {
            result.set(frustum.getSceneSize());
        }
        return result;
    }

    public void resetCamera() {
        cameraMan.reset();
    }

    public void onDimensionsChanged(@NonNull Dimensions dimensions, @Nullable Object source) {
        if (SOURCE == source) {
            return;
        }
        cameraMan.reset(-dimensions.scene.center.x, -dimensions.scene.center.y);
        Check.isTrue(dimensions.scene.size.width == Frustum.SCENE_WIDTH);
        zoomer.reset();
    }

    private static final class Rotation {

        private static final float MIN_ROTATION = 0.5f;
        private static final Angle DEFAULT_ANGLE = new Angle(0, 0);
        private static final Angle DEFAULT_SPEED = new Angle(0f, 0.5f);

        @NonNull
        final Angle angle;

        @NonNull
        final Angle speed;

        private Rotation() {
            angle = DEFAULT_ANGLE;
            speed = DEFAULT_SPEED;
        }

        private Rotation(@NonNull Bundle bundle) {
            angle = restoreAngle(bundle, "rotation.angle", DEFAULT_ANGLE);
            speed = restoreAngle(bundle, "rotation.speed", DEFAULT_SPEED);
        }

        @NonNull
        private static Angle restoreAngle(@NonNull Bundle bundle, @NonNull String name, @NonNull Angle def) {
            final Parcelable angle = bundle.getParcelable(name);
            if (angle instanceof Angle) {
                return (Angle) angle;
            }
            return def;
        }

        public void saveState(@NonNull Bundle bundle) {
            bundle.putParcelable("rotation.angle", angle);
            bundle.putParcelable("rotation.speed", speed);
        }

        public void onFrame() {
            angle.add(speed);
        }

        public boolean shouldRotate() {
            return Math.abs(speed.x) >= MIN_ROTATION || Math.abs(speed.y) >= MIN_ROTATION;
        }
    }

    private final class RotationHolder {

        /**
         * Synchronization is done on the current instance of the field, so it's always in a good state on GL thread
         */
        @GuardedBy("rotation")
        @NonNull
        Rotation rotation = new Rotation();

        boolean shouldRotate() {
            synchronized (rotation) {
                return rotation.shouldRotate();
            }
        }

        void onFrame(@NonNull GL10 gl) {
            synchronized (rotation) {
                rotation.onFrame();

                float x = -tmp.x;
                float y = -tmp.y;
                float z = 0f;
                final float distance = frustum.distance();

                final boolean reverse = rotation.angle.x >= 90 && rotation.angle.x <= 270;
                final float alphaDegrees = reverse ? 180f + rotation.angle.y : rotation.angle.y;
                final float betaDegrees = reverse ? -(180f + rotation.angle.x) : rotation.angle.x;

                final float alpha = (float) (-Math.PI * alphaDegrees / 180f);
                final float beta = (float) (Math.PI * betaDegrees / 180f);
                final float eyeX = (float) (x + distance * Math.sin(alpha) * Math.cos(beta));
                final float eyeY = (float) (y + distance * Math.sin(beta));
                final float eyeZ = (float) (z + distance * Math.cos(alpha) * Math.cos(beta));

                GLU.gluLookAt(gl, eyeX, eyeY, eyeZ, x, y, z, 0, reverse ? -1 : 1, 0);
            }
        }

        void setRotationSpeed(float angleX, float angleY) {
            synchronized (rotation) {
                rotation.speed.x = angleX;
                rotation.speed.y = angleY;
            }
            view.requestRender();
        }

        void saveState(@NonNull Bundle bundle) {
            synchronized (rotation) {
                rotation.saveState(bundle);
            }
        }

        void restoreState(@NonNull Bundle bundle) {
            synchronized (rotation) {
                rotation = new Rotation(bundle);
                rotating = rotation.shouldRotate();
            }
        }

        public void setRotationTo(float x, float y) {
            synchronized (rotation) {
                rotation.angle.x = x;
                rotation.angle.y = y;
            }
            view.requestRender();
        }
    }

    private final class CameraManHolder {
        @GuardedBy("PlotRenderer.this.lock")
        @NonNull
        private final CameraMan cameraMan = new CameraMan();
        @GuardedBy("PlotRenderer.this.lock")
        private boolean moving;

        public void reset() {
            reset(0f, 0f);
        }

        public void reset(float x, float y) {
            synchronized (lock) {
                if (camera.x != x || camera.y != y) {
                    cameraMan.move(camera, new PointF(x, y));
                    fader.fadeOut();
                    moving = true;
                    view.requestRender();
                }
            }
        }

        public void onFrame(@NonNull PointF out, @NonNull Plotter plotter) {
            synchronized (lock) {
                if (!moving) {
                    out.set(camera);
                    return;
                }

                if (cameraMan.onFrame()) {
                    camera.set(cameraMan.getPosition());
                    view.requestRender();
                } else if (moving) {
                    camera.set(cameraMan.getPosition());
                    moving = false;
                    plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter(camera));
                    fader.fadeIn();
                }
                out.set(camera);
            }
        }

        public void saveState(@NonNull Bundle bundle) {
            synchronized (lock) {
                bundle.putFloat("camera.x", camera.x);
                bundle.putFloat("camera.y", camera.y);
            }
        }

        public void restoreState(@NonNull Bundle bundle) {
            synchronized (lock) {
                camera.x = bundle.getFloat("camera.x", camera.x);
                camera.y = bundle.getFloat("camera.y", camera.y);
            }
        }
    }

    private final class FaderHolder {

        @GuardedBy("PlotRenderer.this.lock")
        @NonNull
        private final Fader fader = new Fader();

        float onFrame() {
            synchronized (lock) {
                if (fader.onFrame()) {
                    view.requestRender();
                }
                return fader.getAlpha();
            }
        }

        public void fadeOut() {
            synchronized (lock) {
                fader.fadeOut();
                view.requestRender();
            }
        }

        public void fadeIn() {
            synchronized (lock) {
                fader.fadeIn();
                view.requestRender();
            }
        }
    }

    private final class ZoomerHolder {

        @NonNull
        private final String TAG = Plot.getTag("Zoomer");

        @GuardedBy("this")
        @NonNull
        volatile Zoomer zoomer = new Zoomer();

        @GuardedBy("this")
        volatile boolean pinchZoom;

        void onFrame(@NonNull GL11 gl, @NonNull Plotter plotter) {
            synchronized (this) {
                if (zoomer.onFrame()) {
                    initFrustum(gl, zoomer.current());

                    // if we were running and now we are stopped it's time to update the dimensions
                    if (!zoomer.isZooming()) {
                        if (!pinchZoom) {
                            plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
                            fader.fadeIn();
                        }
                        startRotating();
                    } else {
                        view.requestRender();
                    }
                } else {
                    initFrustum(gl, zoomer.current());
                }
            }
        }

        void onSurfaceChanged(GL10 gl) {
            synchronized (this) {
                initFrustum(gl, zoomer.current(), true);
            }
        }

        void saveState(@NonNull Bundle bundle) {
            synchronized (this) {
                Log.d(TAG, "Saving state: " + zoomer);
                zoomer.saveState(bundle);
            }
        }

        void restoreState(@NonNull Bundle bundle) {
            synchronized (this) {
                zoomer = new Zoomer(bundle);
                Log.d(TAG, "Restoring state: " + zoomer);
            }

            if (!viewSize.isEmpty()) {
                final Plotter plotter = getPlotter();
                if (plotter != null) {
                    plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
                }
            }
        }

        void zoom(boolean in) {
            final Plotter plotter = getPlotter();
            if (plotter == null) {
                return;
            }

            synchronized (this) {
                if (zoomer.zoom(in, plotter.getDimensions())) {
                    view.requestRender();
                    fader.fadeOut();
                }
                if (in) {
                    Log.d(TAG, "Zooming in: " + zoomer);
                } else {
                    Log.d(TAG, "Zooming out: " + zoomer);
                }
            }
        }

        public boolean canZoom(boolean in) {
            final Plotter plotter = getPlotter();
            if (plotter == null) {
                return true;
            }

            synchronized (this) {
                return zoomer.canZoom(in, plotter.getDimensions());
            }
        }

        void reset() {
            synchronized (this) {
                if (zoomer.reset()) {
                    view.requestRender();
                }
                Log.d(TAG, "Resetting: " + zoomer);
            }
        }

        public void zoomBy(@NonNull ZoomLevels levels) {
            if (!levels.isChanged()) {
                return;
            }

            final Plotter plotter = getPlotter();
            if (plotter == null) {
                return;
            }

            synchronized (this) {
                final float level = levels.getLevel();
                //final boolean zooming = plotter.is3d() ? zoomer.zoomBy(level) : zoomer.zoomBy(levels.x, levels.y);
                final boolean zooming = zoomer.zoomBy(level, plotter.getDimensions());
                if (zooming) {
                    view.requestRender();
                }
                Log.d(TAG, "Zooming by level=" + levels + ". " + zoomer);
            }
        }

        public void setPinchZoom(boolean pinchZoom) {
            synchronized (this) {
                if (this.pinchZoom != pinchZoom) {
                    this.pinchZoom = pinchZoom;
                    final Plotter plotter = getPlotter();
                    if (this.pinchZoom) {
                        if (plotter != null) {
                            plotter.hideCoordinates();
                        }
                        fader.fadeOut();
                        Log.d(TAG, "Starting pinch zoom");
                    } else {
                        fader.fadeIn();
                        if (plotter != null) {
                            plotter.updateScene(SOURCE, viewSize, getSceneSize(), getSceneCenter());
                        }
                        Log.d(TAG, "Ending pinch zoom");
                    }
                }
            }
        }

    }
}
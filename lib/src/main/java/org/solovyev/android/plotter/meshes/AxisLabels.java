package org.solovyev.android.plotter.meshes;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;
import org.solovyev.android.plotter.Plot;
import org.solovyev.android.plotter.text.FontAtlas;
import org.solovyev.android.plotter.text.TextMesh;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL11;

public class AxisLabels extends BaseMesh implements DimensionsAware {

    @NonNull
    private final AxisDirection direction;

    @NonNull
    private final FontAtlas fontAtlas;

    @NonNull
    private final List<FormatInterval> labelFormats = new ArrayList<>();

    @NonNull
    private final PointF camera = new PointF();
    @NonNull
    private final DecimalFormat defaultFormat = new DecimalFormat("##0.##E0");
    private final boolean d3;
    @NonNull
    private final MeshDimensions dimensions;
    private TextMesh meshes;

    {
        labelFormats.add(new FormatInterval(Math.pow(10, -5), Math.pow(10, -3), new DecimalFormat("##0.####")));
        labelFormats.add(new FormatInterval(Math.pow(10, -3), Math.pow(10, -2), new DecimalFormat("##0.###")));
        labelFormats.add(new FormatInterval(Math.pow(10, -2), Math.pow(10, -1), new DecimalFormat("##0.##")));
        labelFormats.add(new FormatInterval(Math.pow(10, -1), Math.pow(10, 2), new DecimalFormat("##0.#")));
        labelFormats.add(new FormatInterval(Math.pow(10, 2), Math.pow(10, 4), new DecimalFormat("##0")));
    }

    private AxisLabels(@NonNull AxisDirection direction, @NonNull FontAtlas fontAtlas, @NonNull Dimensions dimensions, boolean d3) {
        this.direction = direction;
        this.fontAtlas = fontAtlas;
        this.dimensions = new MeshDimensions(dimensions);
        this.d3 = d3;
    }

    @NonNull
    public static AxisLabels x(@NonNull FontAtlas fontAtlas, @NonNull Dimensions dimensions, boolean d3) {
        return new AxisLabels(AxisDirection.X, fontAtlas, dimensions, d3);
    }

    @NonNull
    public static AxisLabels y(@NonNull FontAtlas fontAtlas, @NonNull Dimensions dimensions, boolean d3) {
        return new AxisLabels(AxisDirection.Y, fontAtlas, dimensions, d3);
    }

    @NonNull
    public static AxisLabels z(@NonNull FontAtlas fontAtlas, @NonNull Dimensions dimensions, boolean d3) {
        return new AxisLabels(AxisDirection.Z, fontAtlas, dimensions, d3);
    }

    @NonNull
    public DoubleBufferMesh<AxisLabels> toDoubleBuffer() {
        return DoubleBufferMesh.wrap(this, MySwapper.INSTANCE);
    }

    @Override
    protected void onInit() {
        super.onInit();

        if (dimensions.get().scene.isEmpty()) {
            setDirty();
        }
    }

    @Override
    protected void onInitGl(@NonNull GL11 gl, @NonNull MeshConfig config) {
        super.onInitGl(gl, config);
        final int textureId = fontAtlas.getTextureId();
        if (textureId == -1) {
            return;
        }

        final Dimensions dimensions = this.dimensions.get();

        final float halfSceneWidth = dimensions.scene.size.width / 2;
        final float halfSceneHeight = dimensions.scene.size.height / 2;
        final float sceneX = centerX(dimensions);
        final float sceneY = centerY(dimensions);

        boolean rightEdge = false;
        boolean leftEdge = false;
        boolean topEdge = false;
        boolean bottomEdge = false;

        final boolean isY = direction == AxisDirection.Y;
        final boolean isX = direction == AxisDirection.X;
        final Scene.Axis axis = Scene.Axis.create(dimensions.scene, isY, d3);
        final Scene.Ticks ticks = Scene.Ticks.create(dimensions.graph, axis);
        final float fontScale = 4f * ticks.width / fontAtlas.getFontHeight();
        final int[] dv = direction.vector;
        final int[] da = direction.arrow;
        float x = -dv[0] * (ticks.axisLength / 2 + ticks.step + sceneX - sceneX % ticks.step) + da[0] * ticks.width / 2 + (d3 ? dimensions.scene.center.x : 0);
        if (isY) {
            if (!d3) {
                if (x < -halfSceneWidth - sceneX) {
                    x = -halfSceneWidth - sceneX;
                    leftEdge = true;
                } else if (x > halfSceneWidth - sceneX) {
                    x = halfSceneWidth - sceneX;
                    rightEdge = true;
                }
            }

            if (!leftEdge && !rightEdge) {
                // labels are not on the edge => axis is visible => adjust horizontal position to avoid overlapping with ticks
                x += ticks.width / 2;
            }
        }
        float y = -dv[1] * (ticks.axisLength / 2 + ticks.step + sceneY - sceneY % ticks.step) + da[1] * ticks.width / 2 + (d3 ? dimensions.scene.center.y : 0);
        if (isX && !d3) {
            if (y < -halfSceneHeight - sceneY) {
                y = -halfSceneHeight - sceneY;
                bottomEdge = true;
            } else if (y > halfSceneHeight - sceneY) {
                y = halfSceneHeight - sceneY;
                topEdge = true;
            }
        }
        if (meshes == null) {
            meshes = new TextMesh(6 * ticks.count);
        }
        meshes.reset();
        float z = -dv[2] * (ticks.axisLength / 2 + ticks.step) + da[2] * ticks.width / 2;
        final DecimalFormat format = getFormatter(dimensions.graph.scaleToGraphX(ticks.step));
        TextMesh previous = null;
        boolean previousShifted = false;
        for (int tick = 0; tick < ticks.count; tick++) {
            x += dv[0] * ticks.step;
            y += dv[1] * ticks.step;
            z += dv[2] * ticks.step;

            final String label = getLabel(x, y, z, format, dimensions);
            final TextMesh mesh = fontAtlas.getMesh(label, x, y, z, fontScale, !isY, isY);
            final RectF bounds = mesh.getBounds();
            mesh.translate(0, getVerticalFontOffset(bounds));
            if (direction != AxisDirection.Z && previous != null && !previousShifted) {
                if (RectF.intersects(previous.getBounds(), bounds)) {
                    if (isX) {
                        mesh.translate(0, -ticks.width + bounds.height());
                        previousShifted = true;
                    } else {
                        previousShifted = false;
                        // new label intersects old, let's skip it
                        continue;
                    }
                } else {
                    previousShifted = false;
                }
            } else {
                previousShifted = false;
            }
            if (rightEdge || topEdge) {
                final float dx = rightEdge ? -bounds.width() : 0;
                final float dy = topEdge ? -bounds.height() : 0;
                mesh.translate(dx, dy);
            }
            if (previous != null) {
                fontAtlas.releaseMesh(previous);
            }
            previous = mesh;
            meshes.merge(mesh);
        }
        if (previous != null) {
            fontAtlas.releaseMesh(previous);
        }

        setIndices(meshes.indices, meshes.indicesOrder);
        setVertices(meshes.vertices);
        setTexture(textureId, meshes.textureCoordinates);
    }

    private float getVerticalFontOffset(RectF bounds) {
        return bounds.height() / 6;
    }

    private float centerY(@NonNull Dimensions dimensions) {
        return d3 ? 0 : camera.y - dimensions.scene.center.y;
    }

    private float centerX(@NonNull Dimensions dimensions) {
        return d3 ? 0 : camera.x - dimensions.scene.center.x;
    }

    @NonNull
    private String getLabel(float x, float y, float z, @NonNull DecimalFormat format, @NonNull Dimensions dimensions) {
        final float value;
        switch (direction) {
            case X:
                value = dimensions.graph.toGraphX(x);
                break;
            case Y:
                value = dimensions.graph.toGraphY(y);
                break;
            default:
                value = dimensions.graph.toGraphZ(z);
        }

        // better to add '−' to the font atlas
        return format.format(value).replace('−', '-');
    }

    @NonNull
    private DecimalFormat getFormatter(float step) {
        for (AxisLabels.FormatInterval labelFormat : labelFormats) {
            if (labelFormat.l <= step && step < labelFormat.r) {
                return labelFormat.format;
            }
        }
        return defaultFormat;
    }

    @NonNull
    @Override
    protected BaseMesh makeCopy() {
        return new AxisLabels(direction, fontAtlas, dimensions.get(), d3);
    }

    @NonNull
    @Override
    public Dimensions getDimensions() {
        return this.dimensions.get();
    }

    @Override
    public void setDimensions(@NonNull Dimensions dimensions) {
        if (this.dimensions.set(dimensions)) {
            this.camera.set(Plot.ZERO);
            setDirty();
        }
    }

    public void updateCamera(float dx, float dy) {
        this.camera.offset(dx, dy);
        setDirtyGl();
    }

    @Override
    public String toString() {
        return "AxisLabels{" +
                "direction=" + direction +
                '}';
    }

    private static final class FormatInterval {
        final float l;
        final float r;
        @NonNull
        final DecimalFormat format;

        private FormatInterval(double l, double r, @NonNull DecimalFormat format) {
            this.l = (float) l;
            this.r = (float) r;
            this.format = format;
        }
    }

    public static final class MySwapper implements DoubleBufferMesh.Swapper<AxisLabels> {

        @NonNull
        public static final DoubleBufferMesh.Swapper<AxisLabels> INSTANCE = new MySwapper();

        private MySwapper() {
        }

        @Override
        public void swap(@NonNull AxisLabels current, @NonNull AxisLabels next) {
            next.camera.set(current.camera);
            next.setColor(current.getColor());
            next.setWidth(current.getWidth());
            next.setDimensions(current.getDimensions());
        }
    }

}

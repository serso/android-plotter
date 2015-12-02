package org.solovyev.android.plotter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.solovyev.android.plotter.meshes.MeshSpec;

public class PlotIconView extends View {

    @NonNull
    private final Paint paint = new Paint();

    @Nullable
    private MeshSpec meshSpec;

    public PlotIconView(Context context) {
        super(context);
    }

    public PlotIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlotIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlotIconView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public void setMeshSpec(@Nullable MeshSpec meshSpec) {
        if (equals(this.meshSpec, meshSpec)) {
            return;
        }
        this.meshSpec = meshSpec;
        if (meshSpec != null) {
            this.paint.setStrokeWidth(meshSpec.width);
            this.paint.setColor(meshSpec.color.toInt());
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.meshSpec == null) {
            return;
        }
        final int height = getHeight();
        final int width = getWidth();
        final float y = height / 2 - paint.getStrokeWidth() / 2;
        canvas.drawLine(0, y, width, y, paint);
    }
}

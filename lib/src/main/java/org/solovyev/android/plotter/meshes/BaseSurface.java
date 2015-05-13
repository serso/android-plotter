package org.solovyev.android.plotter.meshes;

import android.util.Log;

import org.solovyev.android.plotter.Dimensions;
import org.solovyev.android.plotter.MeshConfig;
import org.solovyev.android.plotter.Plot;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/*
 0     1     2     3     4     5
  +----->---->v----->---->v-----+
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
11v   10|    9v    8|    7v    6|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
12v   13|   14v   15|   16v   17|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
23v   22|   21v   20|   19v   18|
  +------------------------------
  |     ^     |     ^     |     ^
  |     |     |     |     |     |
24v   25|   26v   27|   28v   29|
  +---->^----->---->^----->---->^
 */
public abstract class BaseSurface extends BaseMesh implements DimensionsAware {

	@Nonnull
	protected volatile Dimensions dimensions;

	@Nonnull
	private final Arrays arrays = new Arrays();

	@Nonnull
	public Dimensions getDimensions() {
		return dimensions;
	}

	protected BaseSurface(@Nonnull Dimensions dimensions) {
		this.dimensions = dimensions;
	}

	public void setDimensions(@Nonnull Dimensions dimensions) {
		// todo serso: might be called on GL thread, requires synchronization
		if (!this.dimensions.equals(dimensions)) {
			this.dimensions = dimensions;
			setDirty();
		}
	}

	@Override
	public void onInit() {
		super.onInit();

		if (!dimensions.isZero()) {
			Log.d(Plot.getTag("Dimensions"), String.valueOf(dimensions));
			createInitializer().init(arrays);
			arrays.createBuffers();
		} else {
			setDirty();
		}
	}

	@Nonnull
	protected abstract SurfaceInitializer createInitializer();

	@Override
	public void onInitGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		super.onInitGl(gl, config);

		setVertices(arrays.getVerticesBuffer());
		setIndices(arrays.getIndicesBuffer(), IndicesOrder.LINE_STRIP);
	}

	@Override
	protected void onPostDraw(@Nonnull GL11 gl) {
		super.onPostDraw(gl);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, arrays.vertices.length / 3);
	}

	protected abstract float z(float x, float y, int xi, int yi);
}

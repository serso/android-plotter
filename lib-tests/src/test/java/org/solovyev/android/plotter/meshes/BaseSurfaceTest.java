package org.solovyev.android.plotter.meshes;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.plotter.Dimensions;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class BaseSurfaceTest {

    @Test
    public void testShouldMakeIndices2x2() throws Exception {
        final short[] indices = fillIndices(2, 2);

        assertEquals(0, indices[0]);
        assertEquals(3, indices[1]);
        assertEquals(2, indices[2]);
        assertEquals(1, indices[3]);
    }

    @Test
    public void testShouldMakeIndices2x3() throws Exception {
        final short[] indices = fillIndices(2, 3);

        assertEquals(0, indices[0]);
        assertEquals(3, indices[1]);
        assertEquals(4, indices[2]);
        assertEquals(5, indices[3]);
        assertEquals(2, indices[4]);
        assertEquals(1, indices[5]);
    }

    @Test
    public void testShouldMakeIndices3x2() throws Exception {
        final short[] indices = fillIndices(3, 2);

        assertEquals(0, indices[0]);
        assertEquals(5, indices[1]);
        assertEquals(4, indices[2]);
        assertEquals(1, indices[3]);
        assertEquals(2, indices[4]);
        assertEquals(3, indices[5]);
    }

    private short[] fillIndices(final int w, final int h) {
        final SurfaceInitializer si = new SurfaceInitializer(new BaseSurface(Dimensions.empty()) {
            @NonNull
            @Override
            protected SurfaceInitializer createInitializer() {
                return null;
            }

            @Override
            protected float z(float x, float y, int xi, int yi) {
                return 0;
            }

            @NonNull
            @Override
            protected BaseMesh makeCopy() {
                return null;
            }
        }, SurfaceInitializer.Data.create(new RectF(-1, -1, 1, 1), w, h));
        final Arrays arrays = new Arrays(w * h * 3, w * h);
        si.init(arrays);
        return arrays.indices;
    }

    @Test
    public void testShouldMakeIndices6x5() throws Exception {
        final short[] indices = fillIndices(6, 5);

        assertEquals(0, indices[0]);
        assertEquals(11, indices[1]);
        assertEquals(12, indices[2]);
        assertEquals(23, indices[3]);
        assertEquals(24, indices[4]);
        assertEquals(25, indices[5]);
        assertEquals(22, indices[6]);
        assertEquals(13, indices[7]);
        assertEquals(10, indices[8]);
        assertEquals(1, indices[9]);
        assertEquals(2, indices[10]);
        assertEquals(9, indices[11]);
        assertEquals(14, indices[12]);
        assertEquals(21, indices[13]);
        assertEquals(26, indices[14]);
        assertEquals(27, indices[15]);
        assertEquals(20, indices[16]);
        assertEquals(15, indices[17]);
        assertEquals(8, indices[18]);
        assertEquals(3, indices[19]);
        assertEquals(4, indices[20]);
        assertEquals(7, indices[21]);
        assertEquals(16, indices[22]);
        assertEquals(19, indices[23]);
        assertEquals(28, indices[24]);
        assertEquals(29, indices[25]);
        assertEquals(18, indices[26]);
        assertEquals(17, indices[27]);
        assertEquals(6, indices[28]);
        assertEquals(5, indices[29]);
    }
}
package org.solovyev.android.plotter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.plotter.meshes.GraphTest;

@RunWith(RobolectricTestRunner.class)
public class DimensionsTest {

	@Test
	public void testScreenToGraphConversion() throws Exception {
		final Dimensions.Scene scene = new Dimensions.Scene();
		scene.setViewDimensions(100, 200);
		Assert.assertEquals(scene.rect.left, scene.toSceneX(0), GraphTest.EPS);
		Assert.assertEquals(scene.rect.right, scene.toSceneX(100), GraphTest.EPS);
		Assert.assertEquals(-scene.rect.top, scene.toSceneY(0), GraphTest.EPS);
		Assert.assertEquals(-scene.rect.bottom, scene.toSceneY(200), GraphTest.EPS);
		Assert.assertEquals(0, scene.toSceneX(100 / 2), GraphTest.EPS);
		Assert.assertEquals(0, scene.toSceneY(200 / 2), GraphTest.EPS);
	}
}

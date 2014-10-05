package org.solovyev.android.plotter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ColorTest {

	@Test
	public void testToInt() throws Exception {
		Assert.assertEquals(android.graphics.Color.RED, Color.RED.toInt());
		Assert.assertEquals(android.graphics.Color.GREEN, Color.GREEN.toInt());
		Assert.assertEquals(android.graphics.Color.BLUE, Color.BLUE.toInt());
		Assert.assertEquals(android.graphics.Color.BLACK, Color.BLACK.toInt());
		Assert.assertEquals(android.graphics.Color.DKGRAY, Color.DKGRAY.toInt());
		Assert.assertEquals(android.graphics.Color.GRAY, Color.GRAY.toInt());
		Assert.assertEquals(android.graphics.Color.LTGRAY, Color.LTGRAY.toInt());
		Assert.assertEquals(android.graphics.Color.WHITE, Color.WHITE.toInt());
		Assert.assertEquals(android.graphics.Color.YELLOW, Color.YELLOW.toInt());
		Assert.assertEquals(android.graphics.Color.CYAN, Color.CYAN.toInt());
		Assert.assertEquals(android.graphics.Color.MAGENTA, Color.MAGENTA.toInt());
		Assert.assertEquals(android.graphics.Color.TRANSPARENT, Color.TRANSPARENT.toInt());
		Assert.assertEquals(0xAABBCCDD, Color.create(0xAABBCCDD).toInt());
	}
}
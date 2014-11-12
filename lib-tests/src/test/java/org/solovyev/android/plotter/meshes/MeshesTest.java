package org.solovyev.android.plotter.meshes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.solovyev.android.plotter.meshes.Meshes.getPower;
import static org.solovyev.android.plotter.meshes.Meshes.getTickStep;

@RunWith(RobolectricTestRunner.class)
public class MeshesTest {
	@Test
	public void testGetPower() throws Exception {
		assertEquals(0, getPower(1f));
		assertEquals(1, getPower(1.1f));
		assertEquals(1, getPower(5f));
		assertEquals(1, getPower(9f));
		assertEquals(1, getPower(9.999f));
		assertEquals(1, getPower(10.0f));
		assertEquals(2, getPower(10.1f));
		assertEquals(2, getPower(100.0f));
		assertEquals(3, getPower(100.1f));
		assertEquals(-1, getPower(0.1f));
		assertEquals(-1, getPower(0.2f));
		assertEquals(-1, getPower(0.5f));
		assertEquals(-1, getPower(0.9f));
		assertEquals(-2, getPower(0.01f));
		assertEquals(-2, getPower(0.09f));
		assertEquals(-3, getPower(0.009f));
		assertEquals(-3, getPower(0.001f));
	}

	@Test
	public void testGetTickStep() throws Exception {
		assertEquals(0.5f, getTickStep(1, 3), 0.0001);
		assertEquals(1f, getTickStep(2, 3), 0.0001);
		assertEquals(0.5f, getTickStep(1.5f, 3), 0.0001);
		assertEquals(0.5f, getTickStep(1.6f, 3), 0.0001);
		assertEquals(0.5f, getTickStep(1.99f, 3), 0.0001);
		assertEquals(10f, getTickStep(51f, 3), 0.0001);
		assertEquals(50f, getTickStep(99f, 3), 0.0001);
		assertEquals(50f, getTickStep(100f, 3), 0.0001);
	}
}
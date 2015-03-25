package org.solovyev.android.plotter.meshes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import javax.annotation.Nonnull;

@RunWith(RobolectricTestRunner.class)
public class GraphTest {

	@Nonnull
	private Graph graph;

	@Before
	public void setUp() throws Exception {
		graph = Graph.create();
		graph.append(1, 1);
		graph.append(2, 2);
		graph.append(3, 3);
		graph.append(4, 4);
	}

	@Test
	public void testShouldMakeSpaceAtTheEnd() throws Exception {
		graph.makeSpaceAtTheEnd();
		verify(graph, 1, 2, 3, 4);
		Assert.assertEquals(4, graph.start);
		Assert.assertEquals(16, graph.end);
	}

	@Test
	public void testShouldAppend() throws Exception {
		graph.append(5, 5);
		verify(graph, 1, 2, 3, 4, 5);
	}

	@Test
	public void testShouldPrepend() throws Exception {
		graph.prepend(0, 0);
		verify(graph, 0, 1, 2, 3, 4);
	}

	@Test
	public void testShouldAddSpaceAtTheEnd() throws Exception {
		graph.clear();

		for(int i = 0; i < 10000; i++) {
			graph.append(i, i);
			final float[] a = new float[i + 1];
			for (int j = 0; j < i + 1; j++) {
				a[j] = j;
			}
			verify(graph, a);
		}
	}

	@Test
	public void testShouldAddSpaceAtTheStart() throws Exception {
		graph.clear();

		for(int i = 0; i < 10000; i++) {
			graph.prepend(i, i);
			final float[] a = new float[i + 1];
			for (int j = 0; j < i + 1; j++) {
				a[j] = i - j;
			}
			verify(graph, a);
		}
	}

	@Test
	public void testShouldAppendPrepend() throws Exception {
		graph.clear();

		for (int i = 1; i < 10; i++) {
			graph.prepend(-i, -i);
		}

		for (int i = 0; i < 100; i++) {
			graph.append(i, i);
		}

		for (int i = 10; i < 1000; i++) {
			graph.prepend(-i, -i);
		}

		for (int i = 100; i < 10000; i++) {
			graph.append(i, i);
		}

		final float[] expected = new float[10000 + 1000 - 1];
		for (int i = 1; i < 11000; i++) {
			expected[i - 1] = i - 1000;
		}

		verify(graph, expected);
	}

	private void verify(@Nonnull Graph graph, float... values) {
		Assert.assertEquals(3 * values.length, graph.length());
		for (int i = 0; i < values.length; i++) {
			final int v = graph.start + 3 * i;
			Assert.assertEquals(values[i], graph.vertices[v], 0.00005f);
			Assert.assertEquals(values[i], graph.vertices[v + 1], 0.00005f);
			Assert.assertEquals(0, graph.vertices[v + 2], 0.00005f);
		}
	}
}
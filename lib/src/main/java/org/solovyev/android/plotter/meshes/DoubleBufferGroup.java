package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;

public final class DoubleBufferGroup<M extends Mesh> implements Group<DoubleBufferMesh<M>> {

	@Nonnull
	private final ListGroup<DoubleBufferMesh<M>> group = ListGroup.create();

	private DoubleBufferGroup() {
	}

	@Nonnull
	public static <M extends Mesh> DoubleBufferGroup<M> create() {
		return new DoubleBufferGroup<M>();
	}

	@Override
	public int size() {
		return group.size();
	}

	@Override
	public void clear() {
		group.clear();
	}

	@Override
	public boolean add(@Nonnull DoubleBufferMesh<M> mesh) {
		return group.add(mesh);
	}

	public boolean addMesh(@Nonnull M mesh) {
		return add(DoubleBufferMesh.wrap(mesh));
	}

	@Override
	@Nonnull
	public DoubleBufferMesh<M> remove(int i) {
		return group.remove(i);
	}

	@Nonnull
	public DoubleBufferMesh<M> get(int location) {
		return group.get(location);
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
		group.draw(gl);
	}

	@Nonnull
	@Override
	public ListGroup<DoubleBufferMesh<M>> copy() {
		return group.copy();
	}

	@Nonnull
	@Override
	public State getState() {
		return group.getState();
	}

	@Override
	public boolean init() {
		return group.init();
	}

	@Override
	public boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		return group.initGl(gl, config);
	}
}

package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.opengles.GL11;

public final class DoubleBufferGroup<M extends Mesh> implements Group<DoubleBufferMesh<M>> {

	@Nonnull
	private final ListGroup<DoubleBufferMesh<M>> group = ListGroup.create();

	@Nullable
	private final DoubleBufferMesh.Swapper<M> swapper;

	private DoubleBufferGroup(@Nonnull DoubleBufferMesh.Swapper<M> swapper) {
		this.swapper = swapper;
	}

	@Nonnull
	public static <M extends Mesh> DoubleBufferGroup<M> create(@Nullable DoubleBufferMesh.Swapper<M> swapper) {
		return new DoubleBufferGroup<M>(swapper);
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
		return add(DoubleBufferMesh.wrap(mesh, swapper));
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
	public void setAlpha(float alpha) {
		group.setAlpha(alpha);
	}

	@Override
	public boolean init() {
		return group.init();
	}

	@Override
	public boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		return group.initGl(gl, config);
	}

	@Override
	public Iterator<DoubleBufferMesh<M>> iterator() {
		return group.iterator();
	}
}

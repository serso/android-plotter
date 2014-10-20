package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.microedition.khronos.opengles.GL11;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

@ThreadSafe
public final class ListGroup<M extends Mesh> implements Group<M> {

	@Nonnull
	private final CopyOnWriteArrayList<M> list;

	private ListGroup() {
		list = new CopyOnWriteArrayList<M>();
	}

	private ListGroup(@Nonnull CopyOnWriteArrayList<M> list) {
		this.list = list;
	}

	@Nonnull
	public static <M extends Mesh> ListGroup<M> create() {
		return new ListGroup<M>();
	}

	@Nonnull
	public static <M extends Mesh> ListGroup<M> create(@Nonnull Collection<M> meshes) {
		return new ListGroup<M>(new CopyOnWriteArrayList<M>(meshes));
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
		for (M mesh : list) {
			mesh.draw(gl);
		}
	}

	@Nonnull
	@Override
	public ListGroup<M> copy() {
		final CopyOnWriteArrayList<M> meshes = new CopyOnWriteArrayList<M>();
		for (M mesh : list) {
			meshes.add((M) mesh.copy());
		}
		return new ListGroup<M>(meshes);
	}

	@Nonnull
	@Override
	public State getState() {
		State state = State.INIT_GL;
		for (Mesh mesh : list) {
			final State meshState = mesh.getState();
			if (state.order > meshState.order) {
				state = meshState;
			}
		}
		return state;
	}

	@Override
	public boolean add(@Nonnull M mesh) {
		return list.add(mesh);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean init() {
		boolean changed = false;
		for (M mesh : list) {
			changed |= mesh.init();
		}
		return changed;
	}

	@Override
	public boolean initGl(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		boolean changed = false;
		for (M mesh : list) {
			changed |= mesh.initGl(gl, config);
		}
		return changed;
	}

	@Override
	@Nonnull
	public M get(int location) {
		return list.get(location);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	@Nonnull
	public M remove(int i) {
		return list.remove(i);
	}

	@Override
	public Iterator<M> iterator() {
		return list.iterator();
	}
}

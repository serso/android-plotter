package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class ListPool<M extends Mesh> implements Pool<M> {

	public ListPool(@Nonnull Callback<M> callback) {
		this.callback = callback;
	}

	@Nonnull
	private final List<M> list = new ArrayList<M>();

	@Nonnull
	private final Callback<M> callback;

	@Nonnull
	@Override
	public M obtain() {
		final int poolSize = list.size();
		final M mesh;
		if (poolSize > 0) {
			mesh = list.remove(poolSize - 1);
		} else {
			mesh = callback.create();
		}
		return mesh;
	}

	@Override
	public void release(@Nonnull M mesh) {
		callback.release(mesh);
		list.add(mesh);
	}

	public static interface Callback<M extends Mesh> {
		@Nonnull
		M create();
		void release(@Nonnull M mesh);
	}
}

package org.solovyev.android.plotter.meshes;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.List;

public class Group implements Mesh {

	@Nonnull
	private final List<Mesh> list;

	public Group() {
		list = new ArrayList<Mesh>();
	}

	public Group(int capacity) {
		list = new ArrayList<Mesh>(capacity);
	}

	@Override
	public void draw(@Nonnull GL10 gl) {
		for (Mesh mesh : list) {
			mesh.draw(gl);
		}
	}

	public boolean add(@Nonnull Mesh mesh) {
		return list.add(mesh);
	}

	public void clear() {
		list.clear();
	}
}

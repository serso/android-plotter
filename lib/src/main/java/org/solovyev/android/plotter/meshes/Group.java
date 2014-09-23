package org.solovyev.android.plotter.meshes;

import org.solovyev.android.plotter.MeshConfig;

import javax.annotation.Nonnull;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Group implements Mesh, Iterable<Mesh> {

	@Nonnull
	private final List<Mesh> list;

	public Group() {
		list = new ArrayList<Mesh>();
	}

	public Group(int capacity) {
		list = new ArrayList<Mesh>(capacity);
	}

	@Override
	public void draw(@Nonnull GL11 gl) {
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

	@Override
	public void init(@Nonnull GL11 gl, @Nonnull MeshConfig config) {
		for (Mesh mesh : list) {
			mesh.init(gl, config);
		}
	}

	@Nonnull
	public Mesh get(int location) {
		return list.get(location);
	}

	public int size() {
		return list.size();
	}

	@Nonnull
	@Override
	public Iterator<Mesh> iterator() {
		return list.iterator();
	}

	@Nonnull
	public Mesh remove(int i) {
		return list.remove(i);
	}
}

package org.solovyev.android.plotter.text;

import android.support.annotation.NonNull;

import org.solovyev.android.plotter.Check;

import java.util.ArrayList;
import java.util.List;

public class ListsPool {

    private static final int MAX_SIZE = 10;
    @NonNull
    private List<List<TextMesh>> pool = new ArrayList<>();

    {
        for (int i = 0; i < MAX_SIZE; i++) {
            pool.add(new ArrayList<TextMesh>());
        }
    }

    @NonNull
    public synchronized List<TextMesh> obtain() {
        final int size = pool.size();
        if (size == 0) {
            return new ArrayList<>();
        }
        return pool.remove(size - 1);
    }

    public synchronized void release(@NonNull List<TextMesh> list) {
        if (pool.size() >= MAX_SIZE) {
            return;
        }
        Check.isTrue(list.size() == 0);
        pool.add(list);
    }
}

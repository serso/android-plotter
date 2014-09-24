package org.solovyev.android.plotter;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

public final class Fps {

	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	private long lastTime = nanoTime();
	private int frames = 0;

	public final void logFrame() {
		frames++;
		final long elapsed = nanoTime() - lastTime;
		if (elapsed >= SECOND) {
			Log.d("FPS", "Fps: " + frames);
			lastTime = nanoTime();
			frames = 0;
		}
	}
}

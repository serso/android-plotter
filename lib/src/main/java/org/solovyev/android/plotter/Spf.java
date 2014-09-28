package org.solovyev.android.plotter;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

/**
 * Seconds per frame
 */
final class Spf {

	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
	private static final long MILLIS = TimeUnit.MILLISECONDS.toNanos(1);
	private long start = 0;
	private long end = 0;
	private int frames = 0;

	public final void logFrameStart() {
		if (end != 0) {
			if (nanoTime() - end >= 100L * MILLIS) {
				// too long pause between frames, probably we are not drawing
				frames = 0;
			}
		}

		if (frames == 0) {
			start = nanoTime();
		}
		frames++;
	}

	public void logFrameEnd() {
		end = nanoTime();
		final long elapsedNanos = end - start;
		if (elapsedNanos >= SECOND) {
			final long elapsedMillis = elapsedNanos / MILLIS;
			final long spf = elapsedMillis / frames;
			Log.d("SPF", "SPF=" + spf + "ms, FPS=" + (1000L / spf));
			frames = 0;
		}
	}
}

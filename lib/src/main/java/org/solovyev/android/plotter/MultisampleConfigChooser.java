package org.solovyev.android.plotter;

import android.opengl.GLSurfaceView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public final class MultisampleConfigChooser implements GLSurfaceView.EGLConfigChooser {

	private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
	private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

	private static final int RED = 5;
	private static final int GREEN = 6;
	private static final int BLUE = 5;
	private static final int DEPTH = 16;

	private boolean usesCoverageAa;

	private final int[] tmp = new int[1];

	@Override
	public EGLConfig chooseConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display) {
		// try to find a normal multisample configuration first.
		ConfigData configData = ConfigData.trySpec(gl, display, EGL10.EGL_RED_SIZE, RED,
				EGL10.EGL_GREEN_SIZE, GREEN,
				EGL10.EGL_BLUE_SIZE, BLUE,
				EGL10.EGL_DEPTH_SIZE, DEPTH,
				EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
				EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
				EGL10.EGL_SAMPLES, 2,
				EGL10.EGL_NONE);

		if (!configData.isValid()) {
			// no normal multisampling config was found. Try to create a
			// coverage multisampling configuration, for the nVidia Tegra2.
			// See the EGL_NV_coverage_sample documentation.

			configData = ConfigData.trySpec(gl, display, EGL10.EGL_RED_SIZE, RED,
					EGL10.EGL_GREEN_SIZE, GREEN,
					EGL10.EGL_BLUE_SIZE, BLUE,
					EGL10.EGL_DEPTH_SIZE, DEPTH,
					EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
					EGL_COVERAGE_BUFFERS_NV, 1 /* true */,
					EGL_COVERAGE_SAMPLES_NV, 2,  // always 5 in practice on tegra 2
					EGL10.EGL_NONE);

			usesCoverageAa = configData.isValid();
		}

		if (!configData.isValid()) {
			// fallback to simple configuration
			configData = ConfigData.trySpec(gl, display,
					EGL10.EGL_RED_SIZE, RED,
					EGL10.EGL_GREEN_SIZE, GREEN,
					EGL10.EGL_BLUE_SIZE, BLUE,
					EGL10.EGL_DEPTH_SIZE, DEPTH,
					EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
					EGL10.EGL_NONE);
		}

		// get all matching configurations
		final EGLConfig[] configs = new EGLConfig[configData.count];
		gl.eglChooseConfig(display, configData.spec, configs, configData.count, tmp);

		final EGLConfig elConfig = findConfig(gl, display, configs);
		if (elConfig == null) {
			throw new IllegalArgumentException("No config chosen");
		}
		return elConfig;
	}

	@Nullable
	private EGLConfig findConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, @Nonnull EGLConfig[] configs) {
		// CAUTION! eglChooseConfigs returns configs with higher bit depth
		// first: Even though we asked for rgb565 configurations, rgb888
		// configurations are considered to be "better" and returned first.
		// You need to explicitly filter the data returned by eglChooseConfig!
		for (final EGLConfig config : configs) {
			if (config != null) {
				final boolean red = findConfigAttrib(gl, display, config, EGL10.EGL_RED_SIZE, 0) == RED;
				final boolean green = findConfigAttrib(gl, display, config, EGL10.EGL_GREEN_SIZE, 0) == GREEN;
				final boolean blue = findConfigAttrib(gl, display, config, EGL10.EGL_BLUE_SIZE, 0) == BLUE;
				final boolean depth = findConfigAttrib(gl, display, config, EGL10.EGL_DEPTH_SIZE, 0) >= DEPTH;
				if (red && green && blue && depth) {
					return config;
				}
			}
		}

		return null;
	}

	private int findConfigAttrib(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, @Nonnull EGLConfig config, int attribute, int defaultValue) {
		if (gl.eglGetConfigAttrib(display, config, attribute, tmp)) {
			return tmp[0];
		}
		return defaultValue;
	}

	private static final class ConfigData {
		final int count;
		final int[] spec;

		private ConfigData(int count, int[] spec) {
			this.count = count;
			this.spec = spec;
		}

		@Nonnull
		private static ConfigData trySpec(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, int... spec) {
			final int[] count = new int[1];
			if (!gl.eglChooseConfig(display, spec, null, 0, count)) {
				return new ConfigData(0, spec);
			}
			return new ConfigData(count[0], spec);
		}

		boolean isValid() {
			return count > 0;
		}
	}
}


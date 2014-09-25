package org.solovyev.android.plotter;

import android.opengl.GLSurfaceView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL configuration chooser which tries to choose a config with anti-aliasing (multisampling).
 * In case if such configuration is not supported by device default configuration is used.
 */
public final class MultisampleConfigChooser implements GLSurfaceView.EGLConfigChooser {

	private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
	private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

	private static final int RED = 5;
	private static final int GREEN = 6;
	private static final int BLUE = 5;
	private static final int ALPHA = 0;
	private static final int DEPTH = 16;

	@Override
	public EGLConfig chooseConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display) {
		// try to find a normal multisample configuration first.
		EGLConfig config = ConfigData.create(EGL10.EGL_RED_SIZE, RED,
				EGL10.EGL_GREEN_SIZE, GREEN,
				EGL10.EGL_BLUE_SIZE, BLUE,
				EGL10.EGL_ALPHA_SIZE, ALPHA,
				EGL10.EGL_DEPTH_SIZE, DEPTH,
				EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
				EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
				EGL10.EGL_SAMPLES, 2,
				EGL10.EGL_NONE).tryConfig(gl, display);
		if (config != null) {
			return config;
		}

		// no normal multisampling config was found. Try to create a
		// coverage multisampling configuration, for the nVidia Tegra2.
		// See the EGL_NV_coverage_sample documentation.
		config = ConfigData.create(EGL10.EGL_RED_SIZE, RED,
				EGL10.EGL_GREEN_SIZE, GREEN,
				EGL10.EGL_BLUE_SIZE, BLUE,
				EGL10.EGL_ALPHA_SIZE, ALPHA,
				EGL10.EGL_DEPTH_SIZE, DEPTH,
				EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
				EGL_COVERAGE_BUFFERS_NV, 1 /* true */,
				EGL_COVERAGE_SAMPLES_NV, 2,  // always 5 in practice on tegra 2
				EGL10.EGL_NONE).tryConfig(gl, display);
		if (config != null) {
			return config;
		}

		// fallback to simple configuration
		config = ConfigData.create(
				EGL10.EGL_RED_SIZE, RED,
				EGL10.EGL_GREEN_SIZE, GREEN,
				EGL10.EGL_BLUE_SIZE, BLUE,
				EGL10.EGL_ALPHA_SIZE, ALPHA,
				EGL10.EGL_DEPTH_SIZE, DEPTH,
				EGL10.EGL_NONE).tryConfig(gl, display);
		if (config != null) {
			return config;
		}

		throw new IllegalArgumentException("No supported configuration found");
	}

	private static int findConfigAttrib(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, @Nonnull EGLConfig config, int attribute, int defaultValue, int[] tmp) {
		if (gl.eglGetConfigAttrib(display, config, attribute, tmp)) {
			return tmp[0];
		}
		return defaultValue;
	}

	private static final class ConfigData {
		final int[] spec;

		private ConfigData(int[] spec) {
			this.spec = spec;
		}

		@Nonnull
		private static ConfigData create(int... spec) {
			return new ConfigData(spec);
		}

		@Nullable
		private EGLConfig tryConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display) {
			final int[] tmp = new int[1];

			if (!gl.eglChooseConfig(display, spec, null, 0, tmp)) {
				return null;
			}
			final int count = tmp[0];
			if (count > 0) {
				// get all matching configurations
				final EGLConfig[] configs = new EGLConfig[count];
				if (!gl.eglChooseConfig(display, spec, configs, count, tmp)) {
					return null;
				}

				return findConfig(gl, display, configs, tmp);
			}

			return null;
		}

		@Nullable
		private EGLConfig findConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, @Nonnull EGLConfig[] configs, int[] tmp) {
			// sometimes eglChooseConfig returns configurations with not requested
			// options: even though we asked for rgb565 configurations, rgb888
			// configurations are considered to be "better" and returned first.
			// We need to explicitly filter data returned by eglChooseConfig
			// adn choose the right configuration.
			for (final EGLConfig config : configs) {
				if (config != null && isDesiredConfig(gl, display, tmp, config)) {
					return config;
				}
			}

			return null;
		}

		private boolean isDesiredConfig(@Nonnull EGL10 gl, @Nonnull EGLDisplay display, @Nonnull int[] tmp, @Nonnull EGLConfig config) {
			for (int i = 0; i + 1 < spec.length; i += 2) {
				final int attribute = spec[i];
				final int desiredValue = spec[i + 1];
				final int actualValue = findConfigAttrib(gl, display, config, attribute, 0, tmp);
				if (attribute == EGL10.EGL_DEPTH_SIZE) {
					if (actualValue < desiredValue) {
						return false;
					}
				} else {
					if (desiredValue != actualValue) {
						return false;
					}
				}
			}

			return true;
		}
	}
}


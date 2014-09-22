package org.solovyev.android.plotter;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public final class MultisampleConfigChooser implements GLSurfaceView.EGLConfigChooser {

	private static final int RED = 5;
	private static final int GREEN = 6;
	private static final int BLUE = 5;
	private static final int DEPTH = 16;

	private int[] config;
	private boolean usesCoverageAa;

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		config = new int[1];

		// Try to find a normal multisample configuration first.
		int[] configSpec = {
				EGL10.EGL_RED_SIZE, RED,
				EGL10.EGL_GREEN_SIZE, GREEN,
				EGL10.EGL_BLUE_SIZE, BLUE,
				EGL10.EGL_DEPTH_SIZE, DEPTH,
				// Requires that setEGLContextClientVersion(2) is called on the view.
				EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
				EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
				EGL10.EGL_SAMPLES, 2,
				EGL10.EGL_NONE
		};

		if (!egl.eglChooseConfig(display, configSpec, null, 0, config)) {
			throw new IllegalArgumentException("eglChooseConfig failed");
		}

		int configsCount = config[0];
		if (configsCount <= 0) {
			// no normal multisampling config was found. Try to create a
			// coverage multisampling configuration, for the nVidia Tegra2.
			// See the EGL_NV_coverage_sample documentation.

			final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
			final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

			configSpec = new int[]{
					EGL10.EGL_RED_SIZE, RED,
					EGL10.EGL_GREEN_SIZE, GREEN,
					EGL10.EGL_BLUE_SIZE, BLUE,
					EGL10.EGL_DEPTH_SIZE, DEPTH,
					EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
					EGL_COVERAGE_BUFFERS_NV, 1 /* true */,
					EGL_COVERAGE_SAMPLES_NV, 2,  // always 5 in practice on tegra 2
					EGL10.EGL_NONE
			};

			if (!egl.eglChooseConfig(display, configSpec, null, 0, config)) {
				throw new IllegalArgumentException("2nd eglChooseConfig failed");
			}
			configsCount = config[0];

			if (configsCount <= 0) {
				// Give up, try without multisampling.
				configSpec = new int[]{
						EGL10.EGL_RED_SIZE, RED,
						EGL10.EGL_GREEN_SIZE, GREEN,
						EGL10.EGL_BLUE_SIZE, BLUE,
						EGL10.EGL_DEPTH_SIZE, DEPTH,
						EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
						EGL10.EGL_NONE
				};

				if (!egl.eglChooseConfig(display, configSpec, null, 0, config)) {
					throw new IllegalArgumentException("3rd eglChooseConfig failed");
				}
				configsCount = config[0];

				if (configsCount <= 0) {
					throw new IllegalArgumentException("No configs match configSpec");
				}
			} else {
				usesCoverageAa = true;
			}
		}

		// get all matching configurations
		final EGLConfig[] configs = new EGLConfig[configsCount];
		if (!egl.eglChooseConfig(display, configSpec, configs, configsCount, config)) {
			throw new IllegalArgumentException("data eglChooseConfig failed");
		}

		// CAUTION! eglChooseConfigs returns configs with higher bit depth
		// first: Even though we asked for rgb565 configurations, rgb888
		// configurations are considered to be "better" and returned first.
		// You need to explicitly filter the data returned by eglChooseConfig!
		int index = -1;
		for (int i = 0; i < configs.length; i++) {
			final boolean red = findConfigAttrib(egl, display, configs[i], EGL10.EGL_RED_SIZE, 0) == RED;
			final boolean green = findConfigAttrib(egl, display, configs[i], EGL10.EGL_GREEN_SIZE, 0) == GREEN;
			final boolean blue = findConfigAttrib(egl, display, configs[i], EGL10.EGL_BLUE_SIZE, 0) == BLUE;
			if (red && green && blue) {
				index = i;
				break;
			}
		}
		final EGLConfig config = configs.length > 0 ? configs[index] : null;
		if (config == null) {
			throw new IllegalArgumentException("No config chosen");
		}
		return config;
	}

	private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
		if (egl.eglGetConfigAttrib(display, config, attribute, this.config)) {
			return this.config[0];
		}
		return defaultValue;
	}
}


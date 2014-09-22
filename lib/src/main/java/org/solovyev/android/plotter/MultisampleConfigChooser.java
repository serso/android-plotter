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

	private static final int RED = 8;
	private static final int GREEN = 8;
	private static final int BLUE = 8;
	private static final int DEPTH = 16;

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

		if (!configData.isValid()) {
			// fallback to even simpler configuration
			configData = ConfigData.trySpec(gl, display,
					EGL10.EGL_RED_SIZE, 5,
					EGL10.EGL_GREEN_SIZE, 6,
					EGL10.EGL_BLUE_SIZE, 5,
					EGL10.EGL_DEPTH_SIZE, DEPTH,
					EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
					EGL10.EGL_NONE);
		}

		if (configData.isValid()) {
			// get all matching configurations
			final EGLConfig[] configs = new EGLConfig[configData.count];
			gl.eglChooseConfig(display, configData.spec, configs, configData.count, tmp);

			final EGLConfig elConfig = findConfig(gl, display, configs);
			if (elConfig == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return elConfig;
		} else {
			// fallback to default android chooser
			final SimpleEGLConfigChooser fallbackChooser = new SimpleEGLConfigChooser(true);
			return fallbackChooser.chooseConfig(gl, display);
		}
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

	/**
	 * Following classes are copied from Android GLSurfaceView
	 */

	private static class SimpleEGLConfigChooser extends ComponentSizeChooser {
		public SimpleEGLConfigChooser(boolean withDepthBuffer) {
			super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0);
		}
	}

	private static class ComponentSizeChooser extends BaseConfigChooser {

		private final int[] mValue;
		// Subclasses can adjust these values:
		protected final int mRedSize;
		protected final int mGreenSize;
		protected final int mBlueSize;
		protected final int mAlphaSize;
		protected final int mDepthSize;
		protected final int mStencilSize;

		public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
									int alphaSize, int depthSize, int stencilSize) {
			super(new int[]{
					EGL10.EGL_RED_SIZE, redSize,
					EGL10.EGL_GREEN_SIZE, greenSize,
					EGL10.EGL_BLUE_SIZE, blueSize,
					EGL10.EGL_ALPHA_SIZE, alphaSize,
					EGL10.EGL_DEPTH_SIZE, depthSize,
					EGL10.EGL_STENCIL_SIZE, stencilSize,
					EGL10.EGL_NONE});
			mValue = new int[1];
			mRedSize = redSize;
			mGreenSize = greenSize;
			mBlueSize = blueSize;
			mAlphaSize = alphaSize;
			mDepthSize = depthSize;
			mStencilSize = stencilSize;
		}

		@Override
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
									  EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);
				if ((d >= mDepthSize) && (s >= mStencilSize)) {
					int r = findConfigAttrib(egl, display, config,
							EGL10.EGL_RED_SIZE, 0);
					int g = findConfigAttrib(egl, display, config,
							EGL10.EGL_GREEN_SIZE, 0);
					int b = findConfigAttrib(egl, display, config,
							EGL10.EGL_BLUE_SIZE, 0);
					int a = findConfigAttrib(egl, display, config,
							EGL10.EGL_ALPHA_SIZE, 0);
					if ((r == mRedSize) && (g == mGreenSize)
							&& (b == mBlueSize) && (a == mAlphaSize)) {
						return config;
					}
				}
			}
			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
									 EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}
	}

	private static abstract class BaseConfigChooser implements GLSurfaceView.EGLConfigChooser {

		protected final int[] mConfigSpec;

		public BaseConfigChooser(int[] configSpec) {
			mConfigSpec = configSpec;
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			int[] num_config = new int[1];
			if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
					num_config)) {
				throw new IllegalArgumentException("eglChooseConfig failed");
			}

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}

			EGLConfig[] configs = new EGLConfig[numConfigs];
			if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
					num_config)) {
				throw new IllegalArgumentException("eglChooseConfig#2 failed");
			}
			EGLConfig config = chooseConfig(egl, display, configs);
			if (config == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return config;
		}

		abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
										EGLConfig[] configs);
	}

}


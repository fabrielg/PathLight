package io.github.fabrielg.pathlight.api;

/**
 * Internal provider that holds the PathLightAPI implementation instance.
 * Set by pathlight-core on startup, read by PathLightAPI.getInstance().
 * External plugins should never use this class directly.
 */
public final class PathLightAPIProvider {

	static PathLightAPI instance = null;

	/**
	 * Called by pathlight-core on startup to register the implementation.
	 */
	public static void register(PathLightAPI api) {
		if (instance != null) {
			throw new IllegalStateException("PathLight API is already registered.");
		}
		instance = api;
	}

	/**
	 * Called by pathlight-core on shutdown to unregister the implementation.
	 */
	public static void unregister() {
		instance = null;
	}

}
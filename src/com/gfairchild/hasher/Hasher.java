package com.gfairchild.hasher;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plugin life cycle.
 */
public class Hasher extends AbstractUIPlugin {
	public static final String PLUGIN_NAME = "Hasher";
	public static final String PLUGIN_ID = "com.gfairchild.Hasher"; // plug-in ID
	private static Hasher plugin; // shared instance

	public Hasher() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return
	 */
	public static Hasher getDefault() {
		return plugin;
	}
}
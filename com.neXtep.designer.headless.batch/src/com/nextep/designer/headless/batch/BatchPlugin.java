package com.nextep.designer.headless.batch;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BatchPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "com.neXtep.designer.headless.batch"; //$NON-NLS-1$
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		BatchPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		BatchPlugin.context = null;
	}

}

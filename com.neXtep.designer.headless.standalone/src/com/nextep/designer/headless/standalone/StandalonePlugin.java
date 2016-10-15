package com.nextep.designer.headless.standalone;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.nextep.datadesigner.exception.ErrorException;

public class StandalonePlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		StandalonePlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		StandalonePlugin.context = null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> serviceInterface) {
		final ServiceReference ref = context.getServiceReference(serviceInterface.getName());
		if (ref != null) {
			Object o = context.getService(ref);
			if (o != null && serviceInterface.isAssignableFrom(o.getClass())) {
				return (T) o;
			}
		}
		throw new ErrorException("Unable to locate requested service " + serviceInterface.getName());
	}
}

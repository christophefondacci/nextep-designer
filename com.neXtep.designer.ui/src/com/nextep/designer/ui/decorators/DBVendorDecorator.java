package com.nextep.designer.ui.decorators;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IVendorOriented;
import com.nextep.designer.ui.helpers.UIHelper;

/**
 * This decorator adds the vendor icon as an overlay on the object
 * 
 * @author Christophe Fondacci
 */
public class DBVendorDecorator implements ILightweightLabelDecorator {

	private final static int quadrant = IDecoration.TOP_LEFT;

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IVendorOriented) {
			final IVendorOriented vendorOriented = (IVendorOriented) element;
			final DBVendor vendor = vendorOriented.getDBVendor();
			if (vendor != null) {
				final Image vendorIcon = UIHelper.getVendorIcon(vendor);
				final ImageDescriptor descriptor = ImageDescriptor.createFromImage(vendorIcon);
				decoration.addOverlay(descriptor, quadrant);
			}
		}
	}

}

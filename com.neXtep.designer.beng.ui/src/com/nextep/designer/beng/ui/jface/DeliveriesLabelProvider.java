/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.ui.jface;

import org.eclipse.swt.graphics.Image;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.ui.BENGImages;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.model.DeliveryRootItem;
import com.nextep.designer.beng.ui.model.DeliveryTypeItem;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.jface.VersionableLabelProvider;

/**
 * The default label provider of the deliveries view
 * 
 * @author Christophe Fondacci
 */
public class DeliveriesLabelProvider extends VersionableLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IVersionInfo) {
			return ((IVersionInfo) element).getLabel();
		} else if (element instanceof DeliveryRootItem) {
			if (((DeliveryRootItem) element).isCurrentWorkspace()) {
				return BengUIMessages.getString("view.deliveries.workspaceDeliveriesNode"); //$NON-NLS-1$
			} else {
				return BengUIMessages.getString("view.deliveries.olderDeliveriesNode"); //$NON-NLS-1$
			}
		} else if (element instanceof DeliveryTypeItem) {
			return ((DeliveryTypeItem) element).getType().getLabel();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IVersionInfo) {
			return VCSImages.ICON_VERSIONTREE;
		} else if (element instanceof DeliveryRootItem) {
			return BENGImages.ICON_DEPLOY_UNIT;
		} else if (element instanceof IDeliveryItem) {
			if (element instanceof IDeliveryModule) {
				return BENGImages.ICON_DELIVERY;
			} else {
				return super.getImage(((IDeliveryItem<?>) element).getContent());
			}
		} else if (element instanceof DeliveryTypeItem) {
			return BENGImages.ICON_CONTAINER;
		}
		return super.getImage(element);
	}
}

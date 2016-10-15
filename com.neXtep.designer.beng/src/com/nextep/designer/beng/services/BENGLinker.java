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
package com.nextep.designer.beng.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.IViewLinker;
import com.nextep.designer.beng.BengMessages;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * @author Christophe Fondacci
 */
public class BENGLinker implements IViewLinker {

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#getLabel()
	 */
	@Override
	public String getLabel() {
		return BengMessages.getString("bengLinker.viewLinker.label"); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#link(com.nextep.designer.vcs.model.IWorkspace)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void link(IWorkspace view) {
		Map<IVersionContainer, IDeliveryModule> deliveriesMap = new HashMap<IVersionContainer, IDeliveryModule>();
		for (IVersionable<?> v : view.getContents()) {
			if (v.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				boolean sandboxed = false;
				final IReference r = v.getReference();
				if (r != null) {
					sandboxed = r.isVolatile();
				}
				List<IDeliveryModule> deliveries = (List<IDeliveryModule>) CorePlugin
						.getIdentifiableDao().loadForeignKey(IDeliveryModule.class, v.getUID(),
								"targetRelease", sandboxed); //$NON-NLS-1$
				if (deliveries != null && deliveries.size() > 0 && !sandboxed) {
					deliveriesMap.put((IVersionContainer) v.getVersionnedObject().getModel(),
							deliveries.iterator().next());
				}
			}
		}
		BENGServices.setDeliveriesMap(deliveriesMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void relink(ITypedObject o, MultiValueMap invRefMap) {
		if (o.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
			boolean sandboxed = false;
			if (o instanceof IReferenceable) {
				final IReference r = ((IReferenceable) o).getReference();
				if (r != null) {
					sandboxed = r.isVolatile();
				}
			}
			List<IDeliveryModule> deliveries = (List<IDeliveryModule>) CorePlugin
					.getIdentifiableDao().loadForeignKey(IDeliveryModule.class,
							((IdentifiedObject) o).getUID(), "targetRelease", sandboxed); //$NON-NLS-1$
			if (deliveries != null && deliveries.size() > 0 && !sandboxed) {
				BENGServices.addDelivery(deliveries.iterator().next());
			}
		}
	}

}

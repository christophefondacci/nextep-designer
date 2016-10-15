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
package com.nextep.designer.beng.providers;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengMessages;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.vcs.model.INamingVariableProvider;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class VersionVariableProvider implements INamingVariableProvider {

	public static final String VAR_NAME = "VERSION"; //$NON-NLS-1$
	
    @Override
	public String getDescription() {
		return BengMessages.getString("namingVariableProvider.version.description"); //$NON-NLS-1$
	}

	@Override
	public String getVariableName() {
		return VAR_NAME;
	}

	@Override
	public String getVariableValue(ITypedObject o) {
		if(o instanceof IVersionable) {
			return ((IVersionable<?>) o).getVersion().getLabel();
		} else if (o instanceof IDeliveryModule) {
			IVersionContainer c = (IVersionContainer)VersionHelper.getReferencedItem(((IDeliveryModule)o).getModuleRef());
			if(c!=null) {
				return VersionHelper.getVersionable(c).getVersion().getLabel();
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean isActiveFor(ITypedObject o) {
		return (o instanceof IVersionable) || (o instanceof IDeliveryModule);
	}

}

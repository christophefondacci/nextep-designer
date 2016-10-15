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

/*
 * Shouldn't this provider be named DeliveryModuleNameProvider since it only applies to DeliveryModule objects?
 */
/**
 * @author Christophe Fondacci
 */
public class ModuleNameProvider implements INamingVariableProvider {

    // TODO [BGA]: the variable name should be centralized in a common interface in order to be
    // referenced by the naming variable providers since some providers share the same variable
    // name.
    public static final String VAR_NAME = "MOD_NAME"; //$NON-NLS-1$

    @Override
	public String getDescription() {
		return BengMessages.getString("namingVariableProvider.moduleName.description"); //$NON-NLS-1$
	}

	@Override
	public String getVariableName() {
		return VAR_NAME;
	}

	@Override
	public String getVariableValue(ITypedObject o) {
		if (isActiveFor(o)) {
	        final IDeliveryModule m = (IDeliveryModule)o;
	        IVersionContainer c = (IVersionContainer)VersionHelper.getReferencedItem(m.getModuleRef());
	        if (c != null) {
	            return c.getName().toLowerCase().replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	        }
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean isActiveFor(ITypedObject o) {
		return (o instanceof IDeliveryModule);
	}

}

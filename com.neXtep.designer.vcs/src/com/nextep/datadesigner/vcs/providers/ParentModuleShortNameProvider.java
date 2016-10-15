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
package com.nextep.datadesigner.vcs.providers;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.INamingVariableProvider;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A provider returning the short name of the container of the specified element if available.
 * 
 * @author Bruno Gautier
 */
public class ParentModuleShortNameProvider implements INamingVariableProvider {

    public static final String VAR_NAME = "MOD_SHORT";

    @Override
    public String getDescription() {
        return VCSMessages.getString("namingVariableProvider.mod_short.description");
    }

    @Override
    public String getVariableName() {
        return VAR_NAME;
    }

    @Override
    public String getVariableValue(ITypedObject o) {
        if (isActiveFor(o)) {
            IVersionContainer container = ((IVersionable<?>)o).getContainer();
            if (container != null) return container.getShortName();
        }
        return "";
    }

    @Override
    public boolean isActiveFor(ITypedObject o) {
        return (o instanceof IVersionable);
    }

}

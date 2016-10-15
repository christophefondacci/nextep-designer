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
package com.nextep.designer.sqlgen.oracle.ui.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This provider is an addition to the standard Generic text provider which adds
 * procedure definition coming from Oracle PL/SQL packages.
 * 
 * @author Christophe Fondacci
 *
 */
public class PackageProceduresTextProvider implements ITypedObjectTextProvider {
	Map<String,ITypedObject> objectsMap;
	@Override
	public ITypedObject getElement(String elementName) {
		return getElementsMap().get(elementName.toUpperCase());
	}

	@Override
	public List<String> listProvidedElements() {
		return new ArrayList<String>(getElementsMap().keySet());
	}

	@Override
	public boolean open(String elementName) {
		final IProcedure p = (IProcedure)getElementsMap().get(elementName);
		if(p!=null) {
			// We need to get the package
			int dotOffset = elementName.indexOf('.');
			if(dotOffset>=0) {
				String packageName = elementName.substring(0,dotOffset);
				// We need the element back
				ITypedObject pkg = SQLEditorUIServices.getInstance().getTypedObjectTextProvider().getElement(packageName);
				if(pkg instanceof IPackage) {
					SQLEditorUIServices.getInstance().openPackageProcedureEditor((IPackage)pkg, p);
					return true;
				}
			}
		}
		return false;
	}

	private Map<String,ITypedObject> getElementsMap() {
		if(objectsMap==null) {
			List<IVersionable<?>> versionables = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),IElementType.getInstance(IPackage.TYPE_ID));
			objectsMap = new HashMap<String, ITypedObject>();
			for(IVersionable<?> v : versionables) {
				final IPackage pkg = (IPackage)v.getVersionnedObject().getModel();
				final Collection<IProcedure> procs = pkg.getProcedures();
				for(IProcedure proc : procs) {
					objectsMap.put(pkg.getName().toUpperCase() + "." + proc.getName().toUpperCase(),proc);
				}
			}
		}
		return objectsMap;
	}
}

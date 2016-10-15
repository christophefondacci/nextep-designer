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
package com.nextep.designer.sqlgen.ui.impl;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionable;

public class GenericTypedObjectTextProvider implements ITypedObjectTextProvider {

	List<ITypedObject> objects = null;

	@Override
	public ITypedObject getElement(String elementName) {
		for (ITypedObject o : getElements()) {
			if (o instanceof IIndex) {
				if (((IIndex) o).getIndexName().equals(elementName)) {
					return o;
				}
			} else if (o instanceof INamedObject) {
				if (elementName.equalsIgnoreCase(((INamedObject) o).getName())) {
					return o;
				}
			}
		}
		return null;
	}

	@Override
	public List<String> listProvidedElements() {
		List<String> names = new ArrayList<String>();
		for (ITypedObject o : getElements()) {
			if (o instanceof IIndex) {
				names.add(((IIndex) o).getIndexName());
			} else if (o instanceof INamedObject) {
				names.add(((INamedObject) o).getName());
			}
		}
		return names;
	}

	private List<ITypedObject> getElements() {
		if (objects == null) {
			List<IVersionable<?>> versionables = VersionHelper.getAllVersionables(
					VersionHelper.getCurrentView(), null);
			objects = new ArrayList<ITypedObject>();
			for (IVersionable<?> v : versionables) {
				if (v.getVersionnedObject().getModel() instanceof IDatabaseObject<?>) {
					objects.add(v);
				}
			}
		}
		return objects;
	}

	@Override
	public boolean open(String elementName) {
		ITypedObject o = getElement(elementName);
		if (o != null) {
			UIControllerFactory.getController(o.getType()).defaultOpen(o);
			return true;
		}
		return false;
	}
}

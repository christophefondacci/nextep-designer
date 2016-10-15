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
package com.nextep.datadesigner.impl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;

/**
 * A property provider for references which displays
 * the internal reference and displays a link to the
 * referenced object.
 *
 * @author Christophe Fondacci
 *
 */
public class ReferencePropertyProvider implements IPropertyProvider {

	private IReference reference;
	private List<IProperty> properties;
	public ReferencePropertyProvider(IReference ref) {
		this.reference=ref;
		properties = new ArrayList<IProperty>();
		properties.add(new Property("Type",ref.getType().getName()));
		properties.add(new Property("UID", ref.getUID().toString()));
		Property refItem = new Property("Referenced item",null);
		properties.add(refItem);
		try {
			List<IReferenceable> refs = CorePlugin.getService(IReferenceManager.class).getReferencedItems(ref);
			for(IReferenceable r : refs) {
				Property item  = null;
				if(r instanceof INamedObject) {
					item = new Property(((INamedObject)r).getName(),null,((ITypedObject)r).getType());
				} else {
					item = new Property(r.toString(),null);
				}
				refItem.addChild(item);
				if(r instanceof IAdaptable) {
					IPropertyProvider provider = (IPropertyProvider)((IAdaptable)r).getAdapter(IPropertyProvider.class);
					if(provider != null) {
						item.addChildren(provider.getProperties());
					}
				} else {
					item.addChild(new Property("<Unviewable properties>",null));
				}
			}
		} catch( Exception e) {
			refItem.addChild(new Property(e.getMessage(),null));
		}
	}
	/**
	 * @see com.nextep.datadesigner.model.IPropertyProvider#getProperties()
	 */
	@Override
	public List<IProperty> getProperties() {
		return properties;
	}

	/**
	 * @see com.nextep.datadesigner.model.IPropertyProvider#setProperty(com.nextep.datadesigner.model.IProperty)
	 */
	@Override
	public void setProperty(IProperty property) {
		// TODO Auto-generated method stub

	}

}

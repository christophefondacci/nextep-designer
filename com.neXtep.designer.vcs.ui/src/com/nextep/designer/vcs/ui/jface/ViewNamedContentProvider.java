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
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A content provider displaying all typed objects of a container.
 * This provider may be extended, mainly to extend showElements 
 * method to provide additional filtering.
 * 
 * @author Christophe Fondacci
 *
 */
public class ViewNamedContentProvider implements
		IStructuredContentProvider {

	private Object[] elements = new Object[] {};
	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return elements;
	}

	/**
	 * A method which indicates if the specified element should
	 * be provided by this content provider.
	 * 
	 * @param eligibleObject eligible object to show
	 * @return <code>true</code> to make this element appear, else <code>false</code>
	 */
	protected boolean showElement(Object eligibleObject) {
		if(eligibleObject instanceof ITypedObject) {
			return UIControllerFactory.getController((ITypedObject) eligibleObject).isEditable();
		}
		return false;
	}
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof IVersionContainer) {
			IVersionContainer c = (IVersionContainer)newInput;
			Collection<INamedObject> elts = new ArrayList<INamedObject>();
			for(IVersionable<?> v : c.getContents()) {
				elts.add(v);
				for(IReferenceable r : v.getReferenceMap().values()) {
					if(r instanceof INamedObject) {
						if(showElement(r)) {
							elts.add((INamedObject)r);
						}
					}
				}
			}
			elements =  elts.toArray();
		} else {
			elements = new Object[] {};
		}
	}

}

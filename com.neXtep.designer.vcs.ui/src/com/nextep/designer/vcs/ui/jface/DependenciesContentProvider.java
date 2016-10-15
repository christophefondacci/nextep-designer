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

import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.ui.impl.TypedNode;
import com.nextep.designer.vcs.ui.model.DependencyMode;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class DependenciesContentProvider implements ITreeContentProvider {

	private final static Log LOGGER = LogFactory.getLog(DependenciesContentProvider.class);
	private IDependencySearchRequest request;

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getChildren(Object parentElement) {
		if (request == null) {
			return null;
		}
		if (parentElement instanceof IDependencySearchRequest) {
			return new Object[] { ((IDependencySearchRequest) parentElement).getElement() };
		} else if (parentElement instanceof ITypedNode) {
			return ((ITypedNode) parentElement).getChildren().toArray();
		} else {
			if (request.getRequestType() == DependencyMode.OBJECTS_DEPENDENT_OF
					&& parentElement instanceof IReferenceable) {
				MultiValueMap invRefMap = request.getReverseDependenciesMap();
				Collection<?> dependencies = invRefMap
						.getCollection(((IReferenceable) parentElement).getReference());
				if (dependencies != null) {
					return TypedNode.buildNodesFromCollection((ITypedObject) parentElement,
							(Collection<ITypedObject>) dependencies, null).toArray();
				}
			} else if (request.getRequestType() == DependencyMode.DIRECT_DEPENDENCIES
					&& parentElement instanceof IReferencer) {
				final IReferencer referencer = (IReferencer) parentElement;
				final Collection<IReference> references = referencer.getReferenceDependencies();
				final Collection<ITypedObject> referencedElts = new LinkedList<ITypedObject>();
				for (IReference r : references) {
					try {
						final IReferenceable referenced = VersionHelper.getReferencedItem(r);
						if (referenced instanceof ITypedObject) {
							referencedElts.add((ITypedObject) referenced);
						}
					} catch (UnresolvedItemException e) {
						LOGGER.error("Unresolved dependencies found on reference " + r.toString(),
								e);
					}
				}
				return TypedNode.buildNodesFromCollection((ITypedObject) parentElement,
						(Collection<ITypedObject>) referencedElts, null).toArray();

			}

		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return true;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IDependencySearchRequest) {
			this.request = (IDependencySearchRequest) newInput;
		}
	}

}

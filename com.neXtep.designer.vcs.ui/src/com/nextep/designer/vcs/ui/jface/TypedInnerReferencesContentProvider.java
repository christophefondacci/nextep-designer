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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * This content provider extracts information from {@link IReferenceContainer} elements and filters
 * specific element types from its input. This provider does not listen anything from its input and
 * is stateless : any change on the input will not affect the contents of an already created
 * provider.
 * 
 * @author Christophe Fondacci
 * @since 1.0.7
 */
public class TypedInnerReferencesContentProvider implements IStructuredContentProvider {

	private IReferenceContainer container;
	private List<IElementType> filteredTypes = Collections.emptyList();
	private Collection<?> removedElements = Collections.emptyList();

	public TypedInnerReferencesContentProvider(IElementType... types) {
		filteredTypes = Arrays.asList(types);
	}

	public TypedInnerReferencesContentProvider(Collection<?> removedElements, IElementType... types) {
		this(types);
		this.removedElements = removedElements;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IReferenceContainer) {
			container = (IReferenceContainer) newInput;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		final List<Object> elements = new ArrayList<Object>();
		if (container != null) {
			// Getting contained referenceable objects
			final Collection<IReferenceable> referenceables = container.getReferenceMap().values();
			// Browsing the list
			for (IReferenceable referenceable : referenceables) {
				// Only considering typed object since we are in a type filter content provider
				if (referenceable instanceof ITypedObject) {
					// Retrieving the object type
					final IElementType type = ((ITypedObject) referenceable).getType();
					// If it matches our filter, we add it to the list
					if (filteredTypes.isEmpty() || filteredTypes.contains(type)) {
						if (!removedElements.contains(referenceable)) {
							elements.add(referenceable);
						}
					}
				}
			}
		}
		return elements.toArray();
	}

}

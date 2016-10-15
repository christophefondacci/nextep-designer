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
import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This content provider will provide elements contained in a given container which match defined
 * types.
 * 
 * @author Christophe
 */
public class TypedContentProvider implements IStructuredContentProvider {

	/** Types to look for */
	private IElementType[] types;

	/**
	 * Initializes a typed content provider. This provider will return contents of the specified
	 * container which match the specified types.
	 * 
	 * @param container container to look into
	 * @param type type to look for
	 */
	public TypedContentProvider(IElementType... type) {
		this.types = type;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement != null && inputElement instanceof IVersionContainer) {
			IVersionContainer container = (IVersionContainer) inputElement;
			List<IVersionable<?>> elements = new ArrayList<IVersionable<?>>();
			if (types.length > 0) {
				for (IElementType t : types) {
					elements.addAll(VersionHelper.getAllVersionables(container, t));
				}
			} else {
				elements.addAll(VersionHelper.getAllVersionables(container, null));
			}
			return elements.toArray();
		}
		// Empty array
		return new Object[] {};
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}

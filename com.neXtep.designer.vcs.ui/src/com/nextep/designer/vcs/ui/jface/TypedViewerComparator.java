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

import org.eclipse.jface.viewers.ViewerSorter;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * @author Christophe Fondacci
 *
 */
public class TypedViewerComparator extends ViewerSorter {

	/**
	 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
	 */
	@Override
	public int category(Object element) {
		if(element instanceof ITypedObject) {
			ITypedObject t = (ITypedObject)element;
			if(t.getType() == IElementType.getInstance("TABLE")) {
				return 0;
			} else if( t.getType() == IElementType.getInstance("SQL_VIEW")) {
				return 1;
			} else if( t.getType() == IElementType.getInstance("FOREIGN_KEY")) {
				return 2;
			} else if( t.getType() == IElementType.getInstance("UNIQUE_KEY")) {
				return 3;
			} else if( t.getType() == IElementType.getInstance("INDEX")) {
				return 4;
			} else {
				return 5;
			}
		}
		return super.category(element);
	}
}

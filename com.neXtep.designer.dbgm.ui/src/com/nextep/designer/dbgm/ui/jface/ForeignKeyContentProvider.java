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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.dbgm.model.ConstraintType;

/**
 * A simple wrapper around a constraint provider setup for a FOREIGN key constraint type. The only
 * purpose of this class is to be instantiable dynamically with an empty constructor from the
 * extension management mechanisms.
 * 
 * @author Christophe Fondacci
 */
public class ForeignKeyContentProvider implements IStructuredContentProvider {

	private IStructuredContentProvider constraintProvider;

	public ForeignKeyContentProvider() {
		constraintProvider = new ConstraintsContentProvider(ConstraintType.FOREIGN);
	}

	@Override
	public void dispose() {
		constraintProvider.dispose();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		constraintProvider.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return constraintProvider.getElements(inputElement);
	}
}

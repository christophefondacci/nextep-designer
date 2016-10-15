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
package com.nextep.designer.sqlgen.ui.navigators;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.sqlgen.model.IPackage;

public class PackageContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		Collection<Object> contents = new ArrayList<Object>();
		if (parentElement instanceof IProcedureContainer) {
			IProcedureContainer pkg = (IProcedureContainer) parentElement;
			contents.addAll(pkg.getProcedures());
		}
		// if (parentElement instanceof IVariableContainer) {
		// final IPackage pkg = (IPackage) parentElement;
		// contents.addAll(pkg.getVariables());
		// }
		return contents.toArray();
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof IPackage);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}

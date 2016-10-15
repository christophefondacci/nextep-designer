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
package com.nextep.designer.data.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;

public class DataDeltaEditorInput implements IEditorInput, IModelOriented<IDataDelta> {

	private IDataDelta delta;
	private IDataSet parentDataSet;

	/**
	 * Creates an input for the data comparator
	 * 
	 * @param dataSet data set for which this data delta has been computed
	 * @param delta the {@link IDataDelta}
	 */
	public DataDeltaEditorInput(IDataSet dataSet, IDataDelta delta) {
		this.delta = delta;
		this.parentDataSet = dataSet;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return parentDataSet.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@Override
	public void setModel(IDataDelta model) {
		this.delta = (IDataDelta) model;
	}

	@Override
	public IDataDelta getModel() {
		return delta;
	}

	public IDataSet getDataSet() {
		return parentDataSet;
	}
}

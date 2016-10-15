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
package com.nextep.datadesigner.vcs.gui.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.vcs.impl.ComparisonPropertyWrapper;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonProperty;


public class ComparisonItemEditorInput implements IEditorInput {
	
	private IComparisonItem comparisonItem;
	private IComparisonProperty comparisonProp;
	public ComparisonItemEditorInput(IComparisonItem item) {
		this.comparisonItem = item;
		comparisonProp = new ComparisonPropertyWrapper("", item);
	}
	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(ImageFactory.ICON_ATTRIBUTE);
	}

	@Override
	public String getName() {
		return comparisonProp.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "";
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public IComparisonItem getComparisonItem() {
		return comparisonItem;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ComparisonItemEditorInput) {
			return comparisonItem == ((ComparisonItemEditorInput)obj).getComparisonItem();
		}
		return false;
	}
}

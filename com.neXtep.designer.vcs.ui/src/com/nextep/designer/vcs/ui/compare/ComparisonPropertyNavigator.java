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
package com.nextep.designer.vcs.ui.compare;

import org.eclipse.swt.widgets.Tree;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.navigators.PropertyNavigator;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.designer.vcs.model.IComparisonProperty;

public class ComparisonPropertyNavigator extends PropertyNavigator {

	public ComparisonPropertyNavigator(IProperty property, Tree parentTree) {
		super(property, parentTree);
	}

	@Override
	public void refreshConnector() {
		if (getModel() instanceof IComparisonProperty) {
			final IComparisonProperty property = (IComparisonProperty) getModel();
			getSWTConnector().setText(2, notNull(property.getComparedValue()));
			switch (property.getDifferenceType()) {
			case MISSING_SOURCE:
				getSWTConnector().setBackground(FontFactory.COMPARISON_ADDED);
				break;
			case MISSING_TARGET:
				getSWTConnector().setBackground(FontFactory.COMPARISON_REMOVED);
				break;
			case DIFFER:
				getSWTConnector().setBackground(FontFactory.COMPARISON_DIFFER);
				break;
			default:
				getSWTConnector().setBackground(null);
				break;
			}
			;
		}
		super.refreshConnector();
	}

	@Override
	protected PropertyNavigator createPropertyNavigator(IProperty p, Tree parent) {
		return new ComparisonPropertyNavigator(p, parent);
	}
}

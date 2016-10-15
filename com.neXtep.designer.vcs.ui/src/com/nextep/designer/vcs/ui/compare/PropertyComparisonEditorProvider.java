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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.vcs.gui.rcp.ComparisonItemEditorInput;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class PropertyComparisonEditorProvider implements IComparisonEditorProvider {

	@Override
	public String getEditorId(IComparisonItem comparisonItem) {
		return PropertyEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(IComparisonItem comparisonItem) {
		return new ComparisonItemEditorInput(comparisonItem);
	}

	@Override
	public Image getIcon() {
		return ImageFactory.ICON_ATTRIBUTE_TINY;
	}

	@Override
	public String getLabel() {
		return VCSUIMessages.getString("comparisonProvider.label.properties");
	}
}

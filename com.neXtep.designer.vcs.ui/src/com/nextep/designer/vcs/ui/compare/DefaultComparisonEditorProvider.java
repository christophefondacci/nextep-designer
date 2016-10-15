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
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class DefaultComparisonEditorProvider extends AbstractComparisonEditorProvider {

	@Override
	public String getEditorId(IComparisonItem compItem, ComparedElement comparedElement) {
		final ITypedObject o = getComparedElement(compItem, comparedElement);
		final ITypedObjectUIController c = UIControllerFactory.getController(o);
		return c.getEditorId();
	}

	@Override
	public IEditorInput getEditorInput(IComparisonItem compItem, ComparedElement comparedElement) {
		final ITypedObject o = getComparedElement(compItem, comparedElement);

		final ITypedObjectUIController c = UIControllerFactory.getController(o);
		final IEditorInput input = c.getEditorInput(o);
		// Trying to get a IComparisonItemEditorInput when available
		return adapt(input, compItem, comparedElement);
	}

	@Override
	public Image getIcon() {
		return ImageFactory.ICON_EDIT_TINY;
	}

	@Override
	public String getLabel() {
		return VCSUIMessages.getString("comparisonProvider.label.editor");
	}
}

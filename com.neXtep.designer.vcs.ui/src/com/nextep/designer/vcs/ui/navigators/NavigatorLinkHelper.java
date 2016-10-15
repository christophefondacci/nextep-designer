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
package com.nextep.designer.vcs.ui.navigators;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class NavigatorLinkHelper implements ILinkHelper {

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		if (aSelection.size() == 1) {
			if (aSelection.getFirstElement() instanceof ITypedObject) {
				final ITypedObject o = (ITypedObject) aSelection.getFirstElement();
				if (!(o instanceof ITypedNode)) {
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(o);
					// final String editorId = controller.getEditorId();
					final IEditorInput input = controller.getEditorInput(o);
					IEditorPart part = aPage.findEditor(input);
					if (part != null) {
						aPage.bringToTop(part);
					}
				}
			}
		}

	}

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (anInput instanceof IModelOriented<?>) {
			IStructuredSelection s = new StructuredSelection(((IModelOriented<?>) anInput)
					.getModel());
			return s;
		}
		return StructuredSelection.EMPTY;
	}

}

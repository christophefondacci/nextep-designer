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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IDependencyUIService;

/**
 * This class handles the link between the currently active editor and the dependencies view. More
 * specifically, this link helper recomputes the dependencies view input when the current editor
 * changes.
 * 
 * @author Christophe Fondacci
 */
public class DependenciesNavigatorLinkHelper implements ILinkHelper {

	private Object lastSelection = null;

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		// Nothing to do as it would be confusing for the user to reveal any editor
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput input) {
		if (input instanceof IModelOriented<?>) {
			final Object model = ((IModelOriented<?>) input).getModel();
			if (model instanceof IReferenceable && !(model instanceof IDiagram)) {
				if (model != lastSelection) {
					lastSelection = model;
					getDependencyUIService().computeDependencies((IReferenceable) model);
					return new StructuredSelection(model);
				}
			}
		}
		return null;
	}

	private IDependencyUIService getDependencyUIService() {
		return VCSUIPlugin.getService(IDependencyUIService.class);
	}
}

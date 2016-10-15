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
package com.nextep.designer.vcs.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.model.ITypedNode;

/**
 * This generic typed element creation handler is used for toolbar contributions where we know we
 * always have container-based elements (whose parent is always a container). This handler will
 * retrieve the parent container from current selection and execute typed element creation.
 * 
 * @author Christophe Fondacci
 */
public class NewContainerBasedTypedInstanceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		IVersionContainer parentContainer = null;
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			for (Object o : sel.toList()) {
				parentContainer = findContainer(o);
				if (parentContainer != null) {
					break;
				}
			}
		}
		if (parentContainer == null) {
			parentContainer = getViewService().getCurrentWorkspace();
		}

		// Creating our new object
		Object o = newInstance(event, parentContainer);
		String noEditionAfterCreation = event.getParameter("com.neXtep.designer.core.noEdition"); //$NON-NLS-1$
		if (noEditionAfterCreation == null || "".equals(noEditionAfterCreation)) { //$NON-NLS-1$
			// Calling default editing action
			UIControllerFactory.getController(o).defaultOpen((ITypedObject) o);
		}
		return null;
	}

	private IVersionContainer findContainer(Object o) {
		IVersionContainer parentContainer = null;
		if (o instanceof IVersionContainer) {
			parentContainer = (IVersionContainer) o;
		} else if (o instanceof IVersionable<?>) {
			parentContainer = ((IVersionable<?>) o).getContainer();
		} else if (o instanceof ITypedNode) {
			final ITypedNode node = (ITypedNode) o;
			parentContainer = findContainer(node.getParent());
		}
		return parentContainer;
	}

	public IWorkspaceService getViewService() {
		return VCSPlugin.getViewService();
	}

	protected Object newInstance(ExecutionEvent event, Object parent) {
		String typeId = event.getParameter("com.neXtep.designer.core.typeId"); //$NON-NLS-1$
		if (typeId == null || "".equals(typeId)) { //$NON-NLS-1$
			throw new ErrorException("No command typeId provided, cannot execute."); //$NON-NLS-1$
		} else {
			if (event.getParameter("nocheck") == null) { //$NON-NLS-1$
				parent = VCSPlugin.getService(IVersioningService.class).ensureModifiable(parent);
			}
			if (parent instanceof IdentifiedObject) {
				volatileCheck(parent);
			}
			return UIControllerFactory.getController(IElementType.getInstance(typeId)).newInstance(
					parent);
		}
	}

	protected void volatileCheck(Object parent) {
		if (((IdentifiedObject) parent).getUID() == null
				|| ((IdentifiedObject) parent).getUID().rawId() == 0) {
			throw new ErrorException(VCSUIMessages.getString("addOnVolatileError")); //$NON-NLS-1$
		}
	}
}

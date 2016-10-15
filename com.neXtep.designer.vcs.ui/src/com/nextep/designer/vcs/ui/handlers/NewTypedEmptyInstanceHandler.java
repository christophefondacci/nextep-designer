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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.model.ITypedNode;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class NewTypedEmptyInstanceHandler extends AbstractHandler {

	private static final Log log = LogFactory.getLog(NewTypedEmptyInstanceHandler.class);

	/**
	 * The constructor.
	 */
	public NewTypedEmptyInstanceHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application
	 * context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		ISelection sel = window.getSelectionService().getSelection();
		if (sel != null && !sel.isEmpty() && sel instanceof IStructuredSelection) {
			Object model = ((IStructuredSelection) sel).getFirstElement();
			if (model instanceof ITypedNode) {
				model = ((ITypedNode) model).getParent();
			}
			if (model instanceof ITypedObject) {
				// Creating our new object
				Object o = newInstance(event, model);
				String noEditionAfterCreation = event
						.getParameter("com.neXtep.designer.core.noEdition");
				if (noEditionAfterCreation == null || "".equals(noEditionAfterCreation)) {
					// Calling default editing action
					UIControllerFactory.getController(o).defaultOpen((ITypedObject) o);
				}
			}
			return null;
		} else {
			log.debug("Empty selection");
			return null;
		}
	}

	protected Object newInstance(ExecutionEvent event, Object parent) {
		String typeId = event.getParameter("com.neXtep.designer.core.typeId"); //$NON-NLS-1$
		if (typeId == null || "".equals(typeId)) { //$NON-NLS-1$
			throw new ErrorException("No command typeId provided, cannot execute.");
		} else {
			if (event.getParameter("nocheck") == null) { //$NON-NLS-1$
				parent = VCSPlugin.getService(IVersioningService.class).ensureModifiable(parent);
				if (parent instanceof ITypedObject) {
					CorePlugin.getPersistenceAccessor().save((ITypedObject) parent);
				}
			}
			if (parent instanceof IdentifiedObject) {
				volatileCheck(parent);
			}
			// Building name
			final IElementType type = IElementType.getInstance(typeId);
			return UIControllerFactory.getController(type).emptyInstance(getAvailableName(type),
					parent);
		}
	}

	private String getAvailableName(IElementType type) {
		int i = 1;
		boolean isAvailable = false;
		final String prefix = type.getId();
		String currentName = prefix;
		while (!isAvailable) {
			currentName = prefix + (i++);
			try {
				final IReferenceable referenceable = CorePlugin.getService(IReferenceManager.class).findByTypeName(
						type, currentName);
				isAvailable = (referenceable == null);
			} catch (ReferenceNotFoundException e) {
				isAvailable = true;
			}
		}
		return currentName;
	}

	protected void volatileCheck(Object parent) {
		if (((IdentifiedObject) parent).getUID() == null
				|| ((IdentifiedObject) parent).getUID().rawId() == 0) {
			throw new ErrorException(VCSUIMessages.getString("addOnVolatileError")); //$NON-NLS-1$
		}
	}
}

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.gui.impl.RenameConnector;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class RenameHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection && !s.isEmpty()) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			Object o = sel.getFirstElement();
			// Checking whether we can modify this element or not
			o = VCSPlugin.getService(IVersioningService.class).ensureModifiable(o);
			// Yes we can...
			if (o instanceof INamedObject && o instanceof IObservable) {
				// Invoking editor through our helper GUIWrapper class
				final RenameConnector connector = new RenameConnector((IObservable) o);
				GUIWrapper wrapper = new GUIWrapper(connector,
						VCSUIMessages.getString("handler.rename.dialogTitle"), //$NON-NLS-1$
						300, 120);
				wrapper.invoke();
				if (!wrapper.isCancelled()) {
					String newName = connector.getNewName();
					if (newName == null || "".equals(newName.trim())) { //$NON-NLS-1$
						MessageDialog.openWarning(wrapper.getShell(),
								VCSUIMessages.getString("handler.rename.invalidTitle"), //$NON-NLS-1$
								VCSUIMessages.getString("handler.rename.invalidMsg")); //$NON-NLS-1$
					} else {
						final INamedObject named = (INamedObject) connector.getModel();
						named.setName(newName);
						return newName;
					}
				}
			}
		}
		return null;
	}
}

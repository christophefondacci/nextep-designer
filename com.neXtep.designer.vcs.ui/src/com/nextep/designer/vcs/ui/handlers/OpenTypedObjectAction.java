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

import java.text.MessageFormat;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class OpenTypedObjectAction extends Action {

	private static final int WARN_DIALOG_ABOVE = 3;
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private final static Log LOGGER = LogFactory.getLog(OpenTypedObjectAction.class);

	public OpenTypedObjectAction(IWorkbenchPage page, ISelectionProvider selProvider) {
		super(UIMessages.getString("command.openEditor")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selProvider;
	}

	@Override
	public boolean isEnabled() {
		final ISelection sel = selectionProvider.getSelection();
		if (!sel.isEmpty()) {
			final IStructuredSelection selection = (IStructuredSelection) sel;
			return selection.size() == 1 && selection.getFirstElement() instanceof ITypedObject
					&& !(selection.getFirstElement() instanceof ITypedNode);
		}
		return false;
	}

	@Override
	public void run() {
		final IStructuredSelection sel = (IStructuredSelection) selectionProvider.getSelection();
		if (!sel.isEmpty()) {
			// Warning before opening too many editors
			if (sel.size() > WARN_DIALOG_ABOVE) {
				final boolean confirmed = MessageDialog.openConfirm(page.getWorkbenchWindow()
						.getShell(), MessageFormat.format(UIMessages
						.getString("editItemWarnCountTitle"), sel.size()), MessageFormat.format( //$NON-NLS-1$
						UIMessages.getString("editItemWarnCount"), sel.size(), sel.size())); //$NON-NLS-1$
				if (!confirmed) {
					return;
				}
			}
			// If OK, we open everything
			final Iterator<?> selIt = sel.iterator();
			while (selIt.hasNext()) {
				final Object o = selIt.next();
				if ((o instanceof ITypedObject) && !(o instanceof IConnector<?, ?>)) {
					final ITypedObject t = (ITypedObject) o;
					UIControllerFactory.getController(t.getType()).defaultOpen(t);
				}
			}
		}
	}

}

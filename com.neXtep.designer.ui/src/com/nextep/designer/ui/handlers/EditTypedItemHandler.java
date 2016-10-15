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
package com.nextep.designer.ui.handlers;

import java.text.MessageFormat;
import java.util.Iterator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;

public class EditTypedItemHandler extends AbstractHandler {

	private static final int WARN_DIALOG_ABOVE = 3;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			if (!sel.isEmpty()) {
				// Warning before opening too many editors
				if (sel.size() > WARN_DIALOG_ABOVE) {
					final boolean confirmed = MessageDialog.openConfirm(HandlerUtil
							.getActiveShell(event), MessageFormat.format(UIMessages
							.getString("editItemWarnCountTitle"), sel.size()), MessageFormat //$NON-NLS-1$
							.format(UIMessages.getString("editItemWarnCount"), sel.size(), sel //$NON-NLS-1$
									.size()));
					if (!confirmed) {
						return null;
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
		return null;
	}

}

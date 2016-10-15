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
package com.nextep.designer.beng.ui.handlers;

import java.text.MessageFormat;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.ui.BengUIMessages;

/**
 * @author Christophe Fondacci
 */
public class RemoveDeliveryItemHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Retrieving delivery type
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			final IStructuredSelection s = (IStructuredSelection) sel;
			if (s.size() > 1) {
				final boolean confirmed = MessageDialog.openConfirm(HandlerUtil
						.getActiveShell(event), MessageFormat.format(
						BengUIMessages.getString("confirmRemoveDeliveryItemTitle"), s.size()), //$NON-NLS-1$
						MessageFormat.format(
								BengUIMessages.getString("confirmRemoveDeliveryItem"), s.size()) //$NON-NLS-1$
						);
				if (!confirmed) {
					throw new CancelException("Operation cancelled by user.");
				}
			}
			// Removing all selected items
			for (Object o : s.toList()) {
				if (o instanceof IDeliveryItem<?>) {
					IDeliveryItem<?> i = (IDeliveryItem<?>) o;
					IDeliveryModule m = i.getParentModule();
					m.removeDeliveryItem(i);
				}
			}
		}
		return null;
	}

}

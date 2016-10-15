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
package com.nextep.designer.dbgm.ui.handlers;

import java.text.MessageFormat;
import java.util.Iterator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.gef.editors.ConnectionEditPart;
import com.nextep.designer.dbgm.gef.editors.DiagramItemPart;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IDiagramItem;

public class EditDiagramItemHandler extends AbstractHandler {

	/**
	 * When selection contains more than this value, a confirmation will be required to open editors
	 */
	private static final int WARN_DIALOG_ABOVE = 3;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			if (!sel.isEmpty()) {
				if (sel.size() > WARN_DIALOG_ABOVE) {
					final boolean confirmed = MessageDialog.openConfirm(
							HandlerUtil.getActiveShell(event),
							MessageFormat.format(
									DBGMUIMessages.getString("editDiagramItemWarnCountTitle"),
									sel.size()),
							MessageFormat.format(
									DBGMUIMessages.getString("editDiagramItemWarnCount"),
									sel.size(), sel.size()));
					if (!confirmed) {
						return null;
					}
				}
				for (Iterator<?> it = sel.iterator(); it.hasNext();) {
					final Object o = it.next();
					if (o instanceof DiagramItemPart || o instanceof ConnectionEditPart) {
						Object model = ((GraphicalEditPart) o).getModel();
						Object innerModel = model;
						if (model instanceof IDiagramItem) {
							innerModel = ((IDiagramItem) model).getItemModel();
						}
						if (innerModel instanceof ITypedObject) {
							UIControllerFactory
									.getController(((ITypedObject) innerModel).getType())
									.defaultOpen((ITypedObject) innerModel);
						}

					}

				}
			}
		}
		return null;
	}

}

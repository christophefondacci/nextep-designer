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
package com.nextep.designer.sqlclient.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.sqlclient.ui.helpers.ExportHelper;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

/**
 * This handler copies the selected SQL rows from the current selection provider and serializes them
 * in CSV to the system clipboard.
 * 
 * @author Christophe Fondacci
 */
public class CopyDatalineHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection s = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().getSelection();
		if (!s.isEmpty() && s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			// Preparing our buffer
			final StringBuffer buf = new StringBuffer(500);
			// Iterating over selected lines
			for (Object o : sel.toArray()) {
				if (o instanceof ISQLRowResult) {
					buf.append(ExportHelper.buildCSVLine((ISQLRowResult) o));
				}
			}
			// Copying to system clipboard
			Clipboard cb = new Clipboard(Display.getDefault());
			TextTransfer textTransfer = TextTransfer.getInstance();
			cb.setContents(new Object[] { buf.toString() }, new Transfer[] { textTransfer });

		}
		return null;
	}

}

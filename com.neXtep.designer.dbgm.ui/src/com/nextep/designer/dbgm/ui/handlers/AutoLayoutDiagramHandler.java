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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.designer.dbgm.gef.DBGMGraphicalEditor;
import com.nextep.designer.dbgm.gef.editors.DiagramEditPart;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.DiagramEditorInput;
import com.nextep.designer.dbgm.ui.layout.DiagramLayoutService;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

public class AutoLayoutDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Retrieving selected connection
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		IDiagram diagram = null;
		if (sel instanceof IStructuredSelection) {
			Iterator<?> selIt = ((IStructuredSelection) sel).iterator();
			while (selIt.hasNext()) {
				Object o = selIt.next();
				if (o instanceof IDiagram) {
					diagram = (IDiagram) o;
					break;
				} else if (o instanceof DiagramEditPart) {
					diagram = (IDiagram) ((DiagramEditPart) o).getModel();
					break;
				}
			}
		}
		if (diagram != null) {
			diagram = VCSUIPlugin.getVersioningUIService().ensureModifiable(diagram);
			final IDiagram d = diagram;
			CommandProgress.runWithProgress(false, new ICommand() {

				@Override
				public Object execute(Object... parameters) {
					DiagramLayoutService.autoLayout(d);
					return null;
				}

				@Override
				public String getName() {
					return MessageFormat.format(DBGMUIMessages
							.getString("autoLayoutDiagramCommand"), d.getName());
				}
			});

			IEditorPart e = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			// TODO: make this code cleaner
			if (e instanceof DBGMGraphicalEditor) {
				if (e.getEditorInput().equals(new DiagramEditorInput(diagram))) {
					((DBGMGraphicalEditor) e).setDirty(true);
				}
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

}

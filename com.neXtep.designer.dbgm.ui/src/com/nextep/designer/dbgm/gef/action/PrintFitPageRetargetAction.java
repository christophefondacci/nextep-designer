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
package com.nextep.designer.dbgm.gef.action;

import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.IWorkbenchPart;

public class PrintFitPageRetargetAction extends PrintAction {

	public PrintFitPageRetargetAction(IWorkbenchPart part) {
		super(part);
	}
	

	@Override
	public void run() {
//		ISelection s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
//		IDiagram d = null;
//		if(s instanceof IStructuredSelection && !s.isEmpty()) {
//			final IStructuredSelection sel = (IStructuredSelection)s;
//			if(sel.getFirstElement() instanceof IDiagram) {
//				d = (IDiagram)sel.getFirstElement();
//			} else if( sel.getFirstElement() instanceof DiagramEditPart) {
//				d = (IDiagram) ((DiagramEditPart)sel.getFirstElement()).getModel();
//			} else if( sel.getFirstElement() instanceof DiagramItemPart) {
//				final IDiagramItem i = (IDiagramItem) ((DiagramItemPart)sel.getFirstElement()).getModel();
//				d = i.getParentDiagram();
//			}
//		}
//		if(d==null) {
//			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Nothing to print", "Unable to locate a diagram to print");
//			return;
//		}
//		int style = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getStyle();
//		Shell shell = new Shell((style & SWT.MIRRORED) != 0 ? SWT.RIGHT_TO_LEFT : SWT.NONE);
//		GraphicalViewer viewer = new ScrollingGraphicalViewer();
//		viewer.createControl(shell);
//		viewer.setEditDomain(new DefaultEditDomain(null));
//		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
//		viewer.setEditPartFactory(new DBGMEditPartFactory());
//		viewer.setContents(d);
//		viewer.flush();
//		
		GraphicalViewer viewer;
		viewer = (GraphicalViewer)getWorkbenchPart().getAdapter(GraphicalViewer.class);
		
		PrintDialog dialog = new PrintDialog(viewer.getControl().getShell(), SWT.NULL);
		
		PrinterData data = dialog.open();
		if (data != null) {
			PrintGraphicalViewerOperation op = 
						new PrintGraphicalViewerOperation(new Printer(data), viewer);
			op.setPrintMode(PrintFigureOperation.FIT_PAGE);
			op.run(getWorkbenchPart().getTitle());
		}
	}

}

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
///*******************************************************************************
// * Copyright (c) 2003, 2005 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package com.nextep.designer.dbgm.gef.action;
//
//import java.io.InputStream;
//import java.io.ObjectInputStream;
//
//import org.eclipse.gef.DefaultEditDomain;
//import org.eclipse.gef.GraphicalViewer;
//import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
//import org.eclipse.gef.print.PrintGraphicalViewerOperation;
//import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.printing.PrintDialog;
//import org.eclipse.swt.printing.Printer;
//import org.eclipse.swt.printing.PrinterData;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.IObjectActionDelegate;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.PlatformUI;
//
///**
// * @author Eric Bordeau
// */
//public class DiagramPrintAction 
//	extends Action 
//	implements IObjectActionDelegate
//{
//
//private Object contents;
//private IFile selectedFile;
//
//public DiagramPrintAction() {}
//
//protected Object getContents() {
//	return contents;
//}
//
///**
// * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
// */
//public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
//
///**
// * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
// */
//public void run(IAction action) {
//	int style = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getStyle();
//	Shell shell = new Shell((style & SWT.MIRRORED) != 0 ? SWT.RIGHT_TO_LEFT : SWT.NONE);
//	GraphicalViewer viewer = new ScrollingGraphicalViewer();
//	viewer.createControl(shell);
//	viewer.setEditDomain(new DefaultEditDomain(null));
//	viewer.setRootEditPart(new ScalableFreeformRootEditPart());
//	viewer.setEditPartFactory(new GraphicalPartFactory());
//	viewer.setContents(getContents());
//	viewer.flush();
//	
//	int printMode = new PrintModeDialog(shell).open();
//	if (printMode == -1)
//		return;
//	PrintDialog dialog = new PrintDialog(shell, SWT.NULL);
//	PrinterData data = dialog.open();
//	if (data != null) {
//		PrintGraphicalViewerOperation op = 
//					new PrintGraphicalViewerOperation(new Printer(data), viewer);
//		op.setPrintMode(printMode);
//		op.run(selectedFile.getName());
//	}
//}
//
///**
// * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
// */
//public void selectionChanged(IAction action, ISelection selection) {
//	if (!(selection instanceof IStructuredSelection))
//		return;
//	IStructuredSelection sel = (IStructuredSelection)selection;
//	if (sel.size() != 1)
//		return;
//	selectedFile = (IFile)sel.getFirstElement();
//	try {
//		InputStream is = selectedFile.getContents(false);
//		ObjectInputStream ois = new ObjectInputStream(is);
//		setContents(ois.readObject());
//		ois.close();
//	} catch (Exception e) {
//		//This is just an example.  All exceptions caught here.
//		e.printStackTrace();
//	}
//	
//}
//
//protected void setContents(Object o) {
//	contents = o;
//}
//
//}

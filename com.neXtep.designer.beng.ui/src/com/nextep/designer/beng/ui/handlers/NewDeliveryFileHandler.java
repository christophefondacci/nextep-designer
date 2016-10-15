///*******************************************************************************
// * Copyright (c) 2011 neXtep Software and contributors.
// * All rights reserved.
// *
// * This file is part of neXtep designer.
// *
// * NeXtep designer is free software: you can redistribute it 
// * and/or modify it under the terms of the GNU General Public 
// * License as published by the Free Software Foundation, either 
// * version 3 of the License, or any later version.
// *
// * NeXtep designer is distributed in the hope that it will be 
// * useful, but WITHOUT ANY WARRANTY; without even the implied
// * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
// *
// * Contributors:
// *     neXtep Softwares - initial API and implementation
// *******************************************************************************/
//package com.nextep.designer.beng.ui.handlers;
//
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.FileDialog;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.handlers.HandlerUtil;
//import com.neXtep.shared.model.ArtefactType;
//import com.nextep.datadesigner.dbgm.services.DBGMHelper;
//import com.nextep.datadesigner.exception.ErrorException;
//import com.nextep.datadesigner.vcs.services.VCSFiles;
//import com.nextep.designer.beng.model.impl.DeliveryFile;
//import com.nextep.designer.beng.ui.BengUIMessages;
//import com.nextep.designer.beng.ui.model.DeliveryTypeItem;
//import com.nextep.designer.core.model.DBVendor;
//import com.nextep.designer.vcs.model.IRepositoryFile;
//import com.nextep.designer.vcs.ui.impl.ExceptionHandler;
//
///**
// * This handler adds an external file to the selected delivery node. It first prompts the user for a
// * local file (CSV, DAT, SQL), then creates the repository file and wraps a delivery item around it.
// * 
// * @author Christophe Fondacci
// */
//public class NewDeliveryFileHandler extends AbstractHandler {
//
//	// private static final Log log = LogFactory.getLog(NewDeliveryFileHandler.class);
//
//	@Override
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//
//		DeliveryTypeItem typeItem = null;
//		// Retrieving delivery type
//		ISelection sel = HandlerUtil.getCurrentSelection(event);
//		if (sel instanceof IStructuredSelection) {
//			Object o = ((IStructuredSelection) sel).getFirstElement();
//			if (o instanceof DeliveryTypeItem) {
//				typeItem = (DeliveryTypeItem) o;
//			} else {
//				// Exiting with error message
//				throw new ErrorException(BengUIMessages.getString("invalidDeliveryFolder")); //$NON-NLS-1$
//				// return null;
//			}
//		}
//		FileDialog fd = new FileDialog(HandlerUtil.getActiveShell(event));
//		fd.setFilterExtensions(new String[] { "*.dat", "*.csv", "*.sql", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//		final String filePath = fd.open();
//
//		// Querying for script type
//		final Shell shell = new Shell(HandlerUtil.getActiveShell(event), SWT.APPLICATION_MODAL
//				| SWT.CLOSE | SWT.RESIZE);
//		shell.setLayout(new GridLayout(1, false));
//		Label lbl = new Label(shell, SWT.WRAP);
//		lbl.setText(BengUIMessages.getString("addDeliveryFile")); //$NON-NLS-1$
//		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
//		data.widthHint = 250;
//		lbl.setLayoutData(data);
//
//		final Combo combo = new Combo(shell, SWT.READ_ONLY);
//		combo.add("SQL script");
//		combo.setData("SQL script", ArtefactType.SQL);
//		combo.select(0);
//		if (DBGMHelper.getCurrentVendor() == DBVendor.ORACLE) {
//			combo.add("SQL*loader file with control definition");
//			combo.setData("SQL*loader file with control definition", ArtefactType.SQLLOAD);
//		}
//		combo.add("Resource file");
//		combo.setData("Resource file", ArtefactType.RESOURCE);
//		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		Button okButton = new Button(shell, SWT.PUSH);
//		okButton.setText("   OK   ");
//		okButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
//		final DeliveryTypeItem dlvType = typeItem;
//		okButton.addSelectionListener(new SelectionAdapter() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				// Importing file
//				IRepositoryFile repFile = VCSFiles.getInstance().createFromLocalFile(filePath);
//				dlvType.getModule().addDeliveryItem(
//						new DeliveryFile(repFile, dlvType.getType(), (ArtefactType) combo
//								.getData(combo.getText()), null, DBGMHelper.getCurrentVendor()));
//				shell.dispose();
//			}
//		});
//
//		shell.pack();
//		shell.open();
//		Display display = Display.getCurrent();
//		while (!shell.isDisposed()) {
//			try {
//				if (!display.readAndDispatch())
//					display.sleep();
//			} catch (ErrorException e) {
//				// An error might happen, but we should continue anyway
//				ExceptionHandler.handle(e);
//			}
//		}
//		return null;
//	}
//
// }

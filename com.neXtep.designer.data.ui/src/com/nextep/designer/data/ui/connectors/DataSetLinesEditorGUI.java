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
///**
// * Copyright (c) 2008 neXtep Softwares.
// * All rights reserved. Terms of the neXtep licence
// * are available at http://www.nextep-softwares.com
// */
//package com.nextep.designer.data.ui.connectors;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableItem;
//import com.nextep.datadesigner.gui.impl.ImageFactory;
//import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
//import com.nextep.datadesigner.gui.model.IDisplayConnector;
//import com.nextep.datadesigner.model.ChangeEvent;
//import com.nextep.datadesigner.model.IElementType;
//import com.nextep.datadesigner.model.IObservable;
//import com.nextep.designer.core.factories.ControllerFactory;
//import com.nextep.designer.dbgm.model.IDataLine;
//import com.nextep.designer.dbgm.model.IDataSet;
//import com.nextep.designer.dbgm.ui.DBGMImages;
//import com.nextep.designer.ui.factories.UIControllerFactory;
//import com.nextep.designer.ui.model.ITypedObjectUIController;
//
//public class DataSetLinesEditorGUI extends ControlledDisplayConnector implements SelectionListener {
//
//	private Button newLineButton;
//	private Button delLineButton;
//	private IDisplayConnector linesEditor;
//	private Composite editor;
//
//	public DataSetLinesEditorGUI(IDataSet set, ITypedObjectUIController controller) {
//		super(set, controller);
//	}
//
//	@Override
//	protected Control createSWTControl(Composite parent) {
//		editor = new Composite(parent, SWT.NONE);
//		editor.setLayout(new GridLayout(2, false));
//
//		// Building toolbox
//		Composite toolbox = new Composite(editor, SWT.NONE);
//		GridLayout layout = new GridLayout();
//		layout.marginBottom = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginWidth = 0;
//		layout.numColumns = 3;
//		toolbox.setLayout(layout);
//		toolbox.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
//		newLineButton = new Button(toolbox, SWT.PUSH);
//		newLineButton.setImage(DBGMImages.ICON_NEWDATALINE);
//		newLineButton.setToolTipText("Add a new line to this dataset");
//		newLineButton.addSelectionListener(this);
//
//		delLineButton = new Button(toolbox, SWT.PUSH);
//		delLineButton.setImage(ImageFactory.ICON_DELETE);
//		delLineButton.setToolTipText("Removes the selected lines from this dataset");
//		delLineButton.addSelectionListener(this);
//
//		linesEditor = new DataLineEditorGUI((IDataSet) getModel(), null);
//		Control lineControl = linesEditor.create(editor);
//		GridData gridData2 = new GridData();
//		gridData2.horizontalSpan = 2;
//		gridData2.verticalAlignment = GridData.FILL;
//		gridData2.grabExcessHorizontalSpace = true;
//		gridData2.grabExcessVerticalSpace = true;
//		gridData2.horizontalAlignment = GridData.FILL;
//		gridData2.minimumHeight = 150;
//		lineControl.setLayoutData(gridData2);
//		return editor;
//	}
//
//	@Override
//	public Control getSWTConnector() {
//		return editor;
//	}
//
//	@Override
//	public void refreshConnector() {
//		final IDataSet dataSet = (IDataSet) getModel();
//
//		linesEditor.refreshConnector();
//		boolean enab = !dataSet.updatesLocked();
//		newLineButton.setEnabled(enab);
//		delLineButton.setEnabled(enab);
//
//	}
//
//	@Override
//	public void widgetDefaultSelected(SelectionEvent e) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		final IDataSet dataSet = (IDataSet) getModel();
//		if (e.getSource() == newLineButton) {
//			UIControllerFactory.getController(IElementType.getInstance(IDataLine.TYPE_ID))
//					.newInstance(dataSet);
//		} else if (e.getSource() == delLineButton) {
//			Table t = (Table) linesEditor.getSWTConnector();
//			for (TableItem i : t.getSelection()) {
//				IDataLine l = (IDataLine) i.getData();
//				ControllerFactory.getController(l.getType()).modelDeleted(l);
//				i.dispose();
//			}
//			// IdentifiableDAO.getInstance().save(dataSet);
//			linesEditor.refreshConnector();
//		}
//
//	}
//
//	@Override
//	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
//		super.handleEvent(event, source, data);
//		// Dispatching columns addition / removal signals
//		switch (event) {
//		case COLUMN_ADDED:
//		case COLUMN_REMOVED:
//			linesEditor.handleEvent(event, source, data);
//			break;
//		}
//		refreshConnector();
//	}
// }

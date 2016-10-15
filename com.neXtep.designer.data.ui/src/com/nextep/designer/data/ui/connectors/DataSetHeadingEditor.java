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
package com.nextep.designer.data.ui.connectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

public class DataSetHeadingEditor extends ControlledDisplayConnector {

	private Composite editor;
	private Text nameText;
	private Text descText;
	private Text tableText;
	private Button changeTableButton;

	public DataSetHeadingEditor(IDataSet set, ITypedObjectUIController controller) {
		super(set, controller);
	}

	@Override
	public void refreshConnector() {
		final IDataSet set = (IDataSet) getModel();

		nameText.setText(notNull(set.getName()));
		descText.setText(notNull(set.getDescription()));
		final IBasicTable table = set.getTable();
		tableText.setText(table != null ? table.getName() : "");

		final boolean enabled = !set.updatesLocked();
		nameText.setEnabled(enabled);
		descText.setEnabled(enabled);
		changeTableButton.setEnabled(enabled && table == null);
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		addNoMarginLayout(editor, 3);

		Label nameLbl = new Label(editor, SWT.NONE);
		nameLbl.setText("Data set name : ");
		nameLbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));

		nameText = new Text(editor, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Label descLbl = new Label(editor, SWT.NONE);
		descLbl.setText("Description : ");
		descLbl.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		descText = new Text(editor, SWT.BORDER);
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Link tableLbl = new Link(editor, SWT.NONE);
		tableLbl.setText("<a>Table :</a> ");
		tableLbl.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		tableLbl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openDatasetTable();
			}
		});

		tableText = new Text(editor, SWT.BORDER | SWT.INHERIT_FORCE | SWT.TRANSPARENT);
		GridData linkData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		linkData.widthHint = 200;
		tableText.setLayoutData(linkData);
		tableText.setEditable(false);
		tableText.setFont(FontFactory.FONT_BOLD);
		changeTableButton = new Button(editor, SWT.NONE);
		changeTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeTableButton.setToolTipText(DBGMUIMessages.getString("toolTipChangeRemoteTable"));
		changeTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeDatasetTable();
			}
		});
		return editor;
	}

	private void openDatasetTable() {
		final IDataSet set = (IDataSet) getModel();
		final IBasicTable table = set.getTable();
		if (table != null) {
			UIControllerFactory.getController(IElementType.getInstance(IBasicTable.TYPE_ID))
					.defaultOpen(table);
		}
	}

	private void changeDatasetTable() {
		final IDataSet set = (IDataSet) getModel();
		final Object elt = VCSUIPlugin.getService(ICommonUIService.class).findElement(
				changeTableButton.getShell(), "Select the dataset table", //$NON-NLS-1$
				IElementType.getInstance(IBasicTable.TYPE_ID));
		if (elt != null) {
			set.setTable((IBasicTable) elt);
		}
	}
}

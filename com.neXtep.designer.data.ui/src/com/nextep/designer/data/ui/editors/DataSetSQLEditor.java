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
package com.nextep.designer.data.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.data.ui.connectors.DataSetHeadingEditor;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.sqlclient.ui.rcp.SQLFullClientEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;

public class DataSetSQLEditor extends SQLFullClientEditor {

	public final static String EDITOR_ID = "com.neXtep.designer.data.ui.dataSetSqlEditor"; //$NON-NLS-1$
	private IDisplayConnector connector;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		if (input instanceof DataSetSQLEditorInput) {
			final IDataSet set = ((DataSetSQLEditorInput) input).getDataSet();
			connector = new DataSetHeadingEditor(set,
					UIControllerFactory.getController(IElementType.getInstance(IDataSet.TYPE_ID)));
		}
		setHelpContextId("com.neXtep.designer.dbgm.ui.datasetEditor"); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Control c = connector.create(container);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// SQL Client label
		final Label introLbl = new Label(container, SWT.NONE);
		introLbl.setText(DataUiMessages.getString("editor.dataset.sql.queryDatasetIntroLabel")); //$NON-NLS-1$
		Composite clientContainer = new Composite(container, SWT.BORDER);
		FillLayout fl = new FillLayout();
		fl.marginHeight = fl.marginWidth = 0;
		clientContainer.setLayout(fl);
		clientContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createPartControl(clientContainer);
		connector.refreshConnector();
	}

	@Override
	protected String getContributionId() {
		return EDITOR_ID;
	}
}

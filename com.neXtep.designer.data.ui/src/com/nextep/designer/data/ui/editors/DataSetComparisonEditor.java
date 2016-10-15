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

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.connectors.SQLResultConnector;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;

public class DataSetComparisonEditor extends EditorPart implements IUIComponentContainer {

	public static final String EDITOR_ID = "com.neXtep.designer.sqlclient.ui.dataset.comparisonEditor"; //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(DataSetComparisonEditor.class);
	private SQLResultConnector connector;
	private Connection connection;
	private Control control;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Not allowed
	}

	@Override
	public void doSaveAs() {
		// Not allowed
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (input instanceof TypedEditorInput) {
			try {
				connection = DbgmPlugin.getService(IStorageService.class).getLocalConnection();
			} catch (SQLException e) {
				throw new ErrorException(
						DataUiMessages.getString("editor.dataset.comparison.localConnectionFailed") //$NON-NLS-1$
								+ e.getMessage(), e);
			}
			setInput(input);
			setSite(site);
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		connector = new SQLResultConnector();
		connector.setUIComponentContainer(this);
		control = connector.create(parent);

		TypedEditorInput input = (TypedEditorInput) getEditorInput();
		ITypedObject model = input.getModel();
		if (model instanceof IDataSet) {
			final IDataSet dataset = (IDataSet) model;
			final IStorageHandle handle = dataset.getStorageHandle();
			if (handle != null) {
				SQLClientPlugin.getService(ISQLClientService.class).runQuery(connection,
						handle.getSelectStatement(), handle.getDisplayedColumnsCount(), connector);
			}
		}
		setPartName(input.getName());
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	@Override
	public void dispose() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.warn(
						DataUiMessages.getString("editor.dataset.comparison.closeConnectionFailed") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		super.dispose();
	}

	@Override
	public IUIComponent getUIComponent() {
		return connector;
	}

	@Override
	public void run(boolean block, boolean cancellable, IRunnableWithProgress runnable) {

	}

	@Override
	public void setErrorMessage(String message) {
		// No error message support
	}
}

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
/**
 *
 */
package com.nextep.designer.data.ui.controllers;

import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.data.ui.connectors.DataSetEditorGUI;
import com.nextep.designer.data.ui.connectors.DataSetNavigator;
import com.nextep.designer.data.ui.editors.DataSetMultiEditor;
import com.nextep.designer.data.ui.editors.DataSetSQLEditorInput;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.rcp.SQLClientEditorInput;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class DataSetUIController extends AbstractUIController implements ITypedObjectUIController {

	private static final Log LOGGER = LogFactory.getLog(DataSetUIController.class);

	public DataSetUIController() {
		super();
		this.addSaveEvent(ChangeEvent.MODEL_CHANGED);
		this.addSaveEvent(ChangeEvent.COLUMN_ADDED);
		this.addSaveEvent(ChangeEvent.COLUMN_REMOVED);
		// this.addSaveEvent(ChangeEvent.DATALINE_ADDED);
		this.addSaveEvent(ChangeEvent.DATALINE_REMOVED);
		this.addSaveEvent(ChangeEvent.GENERIC_CHILD_ADDED);
		this.addSaveEvent(ChangeEvent.GENERIC_CHILD_REMOVED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new DataSetEditorGUI((IDataSet) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new DataSetNavigator((IDataSet) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		if (!(parent instanceof IBasicTable)) {
			throw new ErrorException(
					DataUiMessages.getString("controller.dataset.noTableFoundException")); //$NON-NLS-1$
		}
		IBasicTable t = (IBasicTable) parent;

		IVersionable<IDataSet> v = VersionableFactory.createVersionable(IDataSet.class);
		IDataSet s = v.getVersionnedObject().getModel();
		// Saving data set TODO check if no regression, else separate setTable from addDataset (see
		// implementation of setTable)
		s.setTable(t);
		newWizardEdition(
				DataUiMessages.getString("controller.dataset.wizardTitle"), initializeEditor(s)); //$NON-NLS-1$
		// Registering the dataset in our container
		IVersionable<IBasicTable> tableVersion = VersionHelper.getVersionable(t);
		tableVersion.getContainer().addVersionable(v, new ImportPolicyAddOnly());
		t.addDataSet(s);

		save(s);
		// //t.addDataSet(s);

		return s;
	}

	@Override
	public SQLClientEditorInput getEditorInput(ITypedObject model) {
		final IDataSet set = (IDataSet) model;
		// Initializing script
		ISQLScript script = CorePlugin.getService(ITypedObjectFactory.class).create(
				ISQLScript.class);
		script.setScriptType(ScriptType.CUSTOM);
		final IStorageHandle handle = set.getStorageHandle();
		if (handle != null) {
			script.appendSQL(handle.getSelectStatement());
		} else {
			final IStorageService storageService = CorePlugin.getService(IStorageService.class);
			final String selectStmt = storageService.getSelectStatement(set);
			script.appendSQL(selectStmt);
		}

		DataSetSQLEditorInput input = new DataSetSQLEditorInput(set, script);
		try {
			final IStorageService storageService = DbgmPlugin.getService(IStorageService.class);
			input.setSqlConnection(storageService.getLocalConnection());
			return input; // new MixedSQLEditorInput(model, input, SQLFullClientEditor.EDITOR_ID,
			// "Query dataset contents in SQL here :");
		} catch (SQLException e) {
			throw new ErrorException(
					DataUiMessages.getString("controller.dataset.localConnectionFailed") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	@Override
	public String getEditorId() {
		return DataSetMultiEditor.EDITOR_ID;
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		// opening editor
		final SQLClientEditorInput input = getEditorInput(model);
		try {
			final IWorkbenchPage page = CoreUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			page.openEditor(input, getEditorId());
			// Ensuring SQL result view is visible
			final ISQLClientService sqlClientService = SQLClientPlugin
					.getService(ISQLClientService.class);
			final IViewPart part = (IViewPart) sqlClientService.getSQLResultViewFor(input, false);
			final IViewSite site = part.getViewSite();
			page.showView(site.getId(), site.getSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);

		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
		final IDataSet set = (IDataSet) model;
		final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		// Launching data load job
		Job j = new Job(DataUiMessages.getString("controller.dataset.loadDataJobTitle")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				dataService.loadDataLinesFromRepository(set, monitor);
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

}

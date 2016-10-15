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
package com.nextep.designer.data.ui.handlers;

import java.sql.Connection;
import java.sql.SQLException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.data.ui.editors.DataSetSQLEditorInput;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

/**
 * This handler saves current local data set back to the repository. If the current dataset is not
 * modifiable (i.e. committed), user will be prompted for checkout.
 * 
 * @author Christophe Fondacci
 */
public class SaveDataSetHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			if (input instanceof DataSetSQLEditorInput) {
				final DataSetSQLEditorInput dataSetInput = (DataSetSQLEditorInput) input;
				final IDataSet currentDataSet = dataSetInput.getDataSet();
				final IDataSet dataSet = VCSPlugin.getService(IVersioningUIService.class)
						.ensureModifiable(currentDataSet);

				// Forcing commit on connection if needed
				final Connection conn = dataSetInput.getSqlConnection();
				try {
					if (conn != null && !conn.getAutoCommit()) {
						conn.commit();
					}
				} catch (SQLException e) {
					throw new ErrorException("Unable to commit current transaction: "
							+ e.getMessage(), e);
				}
				final IDataService dataService = DbgmPlugin.getService(IDataService.class);
				Job j = new BlockingJob("Saving " + dataSet.getName() + " data...") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						dataService.saveDataLinesToRepository(dataSet, monitor);
						return Status.OK_STATUS;
					}
				};
				j.setUser(true);
				j.schedule();
			}
		}
		return null;
	}
}

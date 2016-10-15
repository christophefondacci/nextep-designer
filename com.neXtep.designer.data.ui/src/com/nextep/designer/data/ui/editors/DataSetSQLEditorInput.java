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

import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.sqlclient.ui.rcp.SQLClientEditorInput;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * A dataset SQL input is a specific editor input for a SQL editor client used to query a dataset.
 * In addition to the standard {@link SQLClientEditorInput}, this input holds the reference to the
 * dataset so that it is possible to get the relationship between the editor and the dataset.
 * 
 * @author Christophe Fondacci
 */
public class DataSetSQLEditorInput extends SQLClientEditorInput {

	private IModelOriented<IDataSet> datasetHandle;

	public DataSetSQLEditorInput(IDataSet dataset, ISQLScript script) {
		super(script, null);
		this.datasetHandle = VCSPlugin.getService(IVersioningService.class)
				.createVersionAwareObject(dataset);
	}

	public IDataSet getDataSet() {
		return datasetHandle.getModel();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSetSQLEditorInput) {
			final DataSetSQLEditorInput otherInput = (DataSetSQLEditorInput) obj;
			return getDataSet() == otherInput.getDataSet();
		}
		return false;
	}

}

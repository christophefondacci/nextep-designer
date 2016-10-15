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
package com.nextep.designer.dbgm.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.controllers.base.AbstractVersionableController;

public class DatasetController extends AbstractVersionableController {

	private final static Log log = LogFactory.getLog(DatasetController.class);

	@Override
	public void modelChanged(Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modelDeleted(Object content) {
		IDataSet set = (IDataSet) content;
		try {
			IBasicTable t = set.getTable();
			// VersionHelper.ensureModifiable(t, true);
			t.removeDataSet(set);
			// IdentifiableDAO.getInstance().save(t);
		} catch (ErrorException e) {
			log.warn("Related dataset table not found, skipping.");
		}
		super.modelDeleted(content);
	}
}

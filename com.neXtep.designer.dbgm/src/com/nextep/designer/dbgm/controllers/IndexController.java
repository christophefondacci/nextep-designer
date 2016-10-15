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
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.vcs.controllers.base.AbstractVersionableController;

public class IndexController extends AbstractVersionableController {

	private final static Log LOGGER = LogFactory.getLog(IndexController.class);

	@Override
	public void modelChanged(Object content) {
		save((IdentifiedObject) content);
	}

	@Override
	public void modelDeleted(Object content) {
		final IIndex index = (IIndex) content;
		// Removing versionable
		super.modelDeleted(index);
		// If ok, removing table link
		try {
			index.getIndexedTable().removeIndex(index);
		} catch (ErrorException e) {
			// Non-blocking exception, we should not warn the user as everything is ok
			LOGGER.debug(
					"Problems while removing index " + index.getName() + ": " + e.getMessage(), e);
		}
	}

}

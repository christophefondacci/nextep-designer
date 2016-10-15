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
package com.nextep.designer.vcs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class VersionableController extends AbstractController {

	private final static Log log = LogFactory.getLog(VersionableController.class);

	@Override
	public void modelChanged(Object content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modelDeleted(Object content) {
		IVersionable<?> v = (IVersionable<?>) content;
		IVersionContainer c = v.getContainer();
		if (c != null) {
			VersionHelper.removeVersionable(v);
		} else {
			log.error("Unable to find location of selected versionable.");
		}
	}

}

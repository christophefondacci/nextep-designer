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
package com.nextep.datadesigner.vcs.services;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A service class which provides the action to perform on versionables such as check out, check in,
 * or undo check out, should they require a specific service.
 * 
 * @author Christophe Fondacci
 * @deprecated to be removed for 1.0.5
 */
@Deprecated
public class VersionActions {

	private static final Log log = LogFactory.getLog(VersionActions.class);

	/**
	 * List checkout versionables of the given container.<br>
	 * This method will return checkouts of any sub-container if <code>recurseCotnainers</code> is
	 * set to <code>true</code>
	 * 
	 * @param c container to list checkouts
	 * @param recurseContainers a flag indicating if this method should return checkouts of
	 *        sub-containers.
	 * @return the list of checkouts of the given container
	 */
	public static List<IVersionable<?>> listCheckouts(IVersionContainer c, boolean recurseContainers) {
		List<IVersionable<?>> checkedOutItems = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> content : c.getContents()) {
			if (content.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				checkedOutItems.add(content);
			}
			if (recurseContainers
					&& content.getVersionnedObject().getModel() instanceof IVersionContainer) {
				checkedOutItems.addAll(listCheckouts((IVersionContainer) content
						.getVersionnedObject().getModel(), recurseContainers));
			}
		}
		return checkedOutItems;
	}

}

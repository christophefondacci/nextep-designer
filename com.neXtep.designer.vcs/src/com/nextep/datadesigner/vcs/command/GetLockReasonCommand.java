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
package com.nextep.datadesigner.vcs.command;

import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This command returns the reason why a versioned 
 * element is locked. If the element is a non versionable
 * element (not implementing {@link IVersionable}), this command
 * returns null. If the element is not locked, this command 
 * returns null.<br>
 * Parameter is the versionable object to check.
 * 
 * @author Christophe Fondacci
 *
 */
public class GetLockReasonCommand implements ICommand {

	/**
	 * @see com.nextep.datadesigner.model.ICommand#execute(java.lang.Object[])
	 */
	@Override
	public Object execute(Object... parameters) {
		Object o = parameters[0];
		// If non versionable, returning
		if(!(o instanceof IVersionable)) {
			return null;
		} else {
			IVersionable<?> v = (IVersionable<?>)o;
			// If not locked, returning 
			if(!v.getVersionnedObject().updatesLocked()) {
				return null;
			} else {
				if(v.getVersion().getStatus()!=IVersionStatus.CHECKED_IN && v.getVersion().getUser() != VersionHelper.getCurrentUser()) {
					return v.getType().getName() + " is locked by " + v.getVersion().getUser().getName();  
				}
			}
		}		
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.model.ICommand#getName()
	 */
	@Override
	public String getName() {
		return "Retrieving lock information";
	}

}

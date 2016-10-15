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
package com.nextep.designer.beng.model.impl;

import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

public class ModuleDeliveryIncrement implements IDeliveryIncrement {

	private IVersionInfo from;
	private IVersionInfo to;
	private IVersionContainer module;
	public ModuleDeliveryIncrement(IVersionContainer module, IVersionInfo from, IVersionInfo to) {
		this.module=module;
		this.from=from;
		this.to=to;
	}
	/* (non-Javadoc)
	 * @see com.nextep.datadesigner.beng.gui.service.IDeliveryIncrement#getFromRelease()
	 */
	public IVersionInfo getFromRelease() {
		return from;
	}
	/* (non-Javadoc)
	 * @see com.nextep.datadesigner.beng.gui.service.IDeliveryIncrement#getToRelease()
	 */
	public IVersionInfo getToRelease() {
		return to;
	}
	/* (non-Javadoc)
	 * @see com.nextep.datadesigner.beng.gui.service.IDeliveryIncrement#getModule()
	 */
	public IVersionContainer getModule() {
		return module;
	}
}

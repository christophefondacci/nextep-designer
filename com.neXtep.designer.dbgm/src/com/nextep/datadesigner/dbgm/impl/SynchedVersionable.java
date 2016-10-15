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
package com.nextep.datadesigner.dbgm.impl;

import com.nextep.datadesigner.model.ISynchronizable;
import com.nextep.datadesigner.model.SynchStatus;
import com.nextep.datadesigner.vcs.impl.SelfControlVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public abstract class SynchedVersionable<T> extends SelfControlVersionable<T>
		implements ISynchronizable {

	private SynchStatus synched = SynchStatus.UNKNOWN;
	
	/**
	 * @see com.nextep.datadesigner.model.ISynchronizable#getSynchStatus()
	 */
	@Override
	public SynchStatus getSynchStatus() {
		return synched;
	}

	/**
	 * @see com.nextep.datadesigner.model.ISynchronizable#setSynched(boolean)
	 */
	@Override
	public void setSynched(SynchStatus synched) {
		this.synched = synched;

	}

}

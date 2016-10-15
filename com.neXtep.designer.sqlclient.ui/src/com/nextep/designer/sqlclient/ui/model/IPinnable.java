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
package com.nextep.designer.sqlclient.ui.model;

/**
 * This interface describes the ability of a UI component to be pinnable.
 * 
 * @author Christophe Fondacci
 */
public interface IPinnable {

	/**
	 * Informs whether the component is set as pinned.
	 * 
	 * @return <code>true</code> if pinned, else <code>false</code>
	 */
	boolean isPinned();

	/**
	 * Defines the pinned status of this UI component.
	 * 
	 * @param pinned <code>true</code> to pin the UI component, <code>false</code> to unpin
	 */
	void setPinned(boolean pinned);
}

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
package com.nextep.datadesigner.dbgm.gui.editors;

/**
 * An interface which describes input which can be
 * submitted to database
 * @author Christophe
 *
 */
public interface ISubmitable {

	/**
	 * @return the database type corresponding to this submitable object. 
	 * 		   This type is used to retrieve compilation information
	 */
	public abstract String getDatabaseType();
	/**
	 * @return whether or not the submit button should be displayed when this element 
	 * 		   is edited.
	 */
	public abstract boolean showSubmit();
}

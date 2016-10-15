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
package com.nextep.installer.model;

/**
 * The runtime bean for a version.
 * 
 * @author Christophe Fondacci
 *
 */
public interface IRelease extends Comparable<IRelease> {

	public long getId();
	public void setId(long id);
	public int getMajor();
	public void setMajor(int major);
	public int getMinor();
	public void setMinor(int minor);
	public int getIteration();
	public void setIteration(int iteration);
	public int getPatch();
	public void setPatch(int patch);
	public int getRevision();
	public void setRevision(int revision);
}

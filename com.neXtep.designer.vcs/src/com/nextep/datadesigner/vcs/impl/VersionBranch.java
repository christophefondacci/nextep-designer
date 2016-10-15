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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.impl.NamedObjectHelper;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionBranch;

public class VersionBranch extends Observable implements IVersionBranch {
	//private static final Log log = LogFactory.getLog(VersionBranch.class);

	private UID id;
	private static IVersionBranch DEFAULT_BRANCH; // = (IVersionBranch)IdentifiableDAO.getInstance().load(VersionBranch.class,new UID(1)); //new VersionBranch("MAIN","Main branch");
	private NamedObjectHelper nameHelper = null;
	public static IVersionBranch getDefaultBranch() {
		if(DEFAULT_BRANCH == null) {
			DEFAULT_BRANCH = (IVersionBranch)CorePlugin.getIdentifiableDao().load(VersionBranch.class,new UID(1)); //new VersionBranch("MAIN","Main branch");
		}
		return DEFAULT_BRANCH;
	}
	public static void reset() {
		DEFAULT_BRANCH = null;
	}
	public VersionBranch(String name, String description) {
		nameHelper = new NamedObjectHelper(name,description);
	}
	protected VersionBranch() {
		nameHelper = new NamedObjectHelper(null,null);
		//System.out.println("VersionBranch: Empty constructor called");
	}
	@Override
	public String getDescription() {
		return nameHelper.getDescription();
	}

	@Override
	public String getName() {
		return nameHelper.getName();
	}

	@Override
	public void setDescription(String description) {
		nameHelper.setDescription(description);
		//System.out.println("VersionBranch: setDescription(" + description + ")");
	}

	@Override
	public void setName(String name) {
		nameHelper.setName(name);
		//System.out.println("VersionBranch: setName(" + name + ")");
	}
	public UID getUID() {
		return id;
	}
	/**
	 * Defines a new unique identifier of this object
	 *
	 * @param id new identifier for the object
	 */
	protected void setId(long id) {
		//log.debug("Called setId with id="+id);
		this.id=new UID(id);
	}

	protected long getId() {
		if(id == null) {
			return 0;
		}
		return id.rawId();
	}
	public void setUID(UID id) {
		this.id=id;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IVersionBranch) {
			return this.getName().equals(((IVersionBranch)obj).getName());
		}
		return false;
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

}

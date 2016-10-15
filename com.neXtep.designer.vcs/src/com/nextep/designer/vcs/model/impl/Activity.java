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
/**
 *
 */
package com.nextep.designer.vcs.model.impl;

import com.nextep.datadesigner.impl.NamedObjectHelper;
import com.nextep.designer.vcs.model.IActivity;

/**
 * Default implementation of an activity
 * 
 * @author Christophe Fondacci
 */
public class Activity implements IActivity {

	private NamedObjectHelper helper;

	public static IActivity getDefaultActivity() {
		return new Activity("neXtep internal activity");
	}

	public static void reset() {
	}

	public Activity(String name) {
		helper = new NamedObjectHelper(name, null);
	}

	/**
	 * Hibernate empty constructor
	 */
	protected Activity() {
		helper = new NamedObjectHelper(null, null);
	}

	/**
	 * @see com.nextep.datadesigner.model.INamedObject#getDescription()
	 */
	public String getDescription() {
		return helper.getDescription();
	}

	/**
	 * @see com.nextep.datadesigner.model.INamedObject#getName()
	 */
	public String getName() {
		return helper.getName();
	}

	/**
	 * @see com.nextep.datadesigner.model.INamedObject#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		helper.setDescription(description);
	}

	/**
	 * @see com.nextep.datadesigner.model.INamedObject#setName(java.lang.String)
	 */
	public void setName(String name) {
		helper.setName(name);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IActivity) {
			final String n = getName();
			if (n != null) {
				return n.equals(((IActivity) obj).getName());
			} else {
				return ((IActivity) obj).getName() == null;
			}
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

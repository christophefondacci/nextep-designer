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
package com.nextep.designer.vcs.policies;

import com.nextep.designer.vcs.model.IVersionable;

/**
 * This policy allows to define the target object which
 * will be used as the new checkout object.<br>
 * <b>Important</b>: There should be one policy instance per checkout
 * since this policy holds a specific target object.
 * 
 * @author Christophe
 *
 */
public class CheckOutInExistingObjectVersionPolicy extends DefaultVersionPolicy {

	private IVersionable<?> targetObject;
	public CheckOutInExistingObjectVersionPolicy(IVersionable<?> targetObject) {
		this.targetObject = targetObject;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <V> IVersionable<V> createCheckedOutObject(IVersionable<V> source) {
		return (IVersionable<V>)targetObject;
	}
}

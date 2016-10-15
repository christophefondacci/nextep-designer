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
package com.nextep.designer.ui.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;

public class ElementTypePropertyTester extends PropertyTester {

	public final static String PROP_TYPE_NAME = "typeId"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IElementType type = null;
		if (receiver == null || expectedValue == null) {
			return false;
		}
		if (receiver instanceof ITypedObject) {
			type = ((ITypedObject) receiver).getType();
		} else if (receiver instanceof IElementType) {
			type = (IElementType) receiver;
		} else if (receiver instanceof IAdaptable) {
			type = (IElementType) ((IAdaptable) receiver).getAdapter(IElementType.class);
		}
		if (type != null) {
			if (PROP_TYPE_NAME.equals(property)) {
				return type.getId().equals(expectedValue);
			}
		}
		return false;
	}
}

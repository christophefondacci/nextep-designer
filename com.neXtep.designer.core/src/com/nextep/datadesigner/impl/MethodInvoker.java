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
package com.nextep.datadesigner.impl;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IInvokable;

/**
 * @author Christophe Fondacci
 *
 */
public abstract class MethodInvoker implements IInvokable {


	/**
	 * This method should return the expected parameter types
	 * @return an array of class corresponding to the correct method argument
	 * 		   types
	 */
	public abstract Class<?>[] getParameterTypes();
	/**
	 * @return the method name
	 */
	public abstract String getMethodName();
	/**
	 * Implements basic checks of argument parameters before delegating
	 * the method call to invokeMethod func.
	 * @see com.nextep.datadesigner.model.IInvokable#invoke(java.lang.Object[])
	 */
	@Override
	public final Object invoke(Object... arg) {
		Class<?>[] parameters = getParameterTypes();
		if(arg.length!= parameters.length) {
			throw new ErrorException("Invalid number of arguments when calling <"+ getMethodName() + ">.");
		}

		for(int i = 0 ; i < parameters.length ; i++){
			if(!parameters[i].isInstance(arg[i]) && arg[i]!=null) {
				throw new ErrorException("Invalid argument type at index " + i + " when calling <" + getMethodName() + ">.");
			}
		}
		return invokeMethod(arg);
	}
	/**
	 * Invokes the method after a few checks have been made.
	 *
	 * @param arg method arguments (as expected)
	 * @return the method return information
	 */
	public abstract Object invokeMethod(Object... arg);
}

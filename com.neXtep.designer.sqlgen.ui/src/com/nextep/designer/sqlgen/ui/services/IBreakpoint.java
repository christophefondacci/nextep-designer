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
package com.nextep.designer.sqlgen.ui.services;

import org.eclipse.jface.text.source.Annotation;
import com.nextep.datadesigner.sqlgen.model.IPackage;

/**
 * A breakpoint definition which is closely
 * tied to the SQLEditor UI.
 * 
 * @author Christophe Fondacci
 *
 */
public interface IBreakpoint {

	/**
	 * TODO: Generalize to support either procedures / functions / package 
	 * @return the target of this breakpoint
	 * 
	 */
	public IPackage getTarget();
	/**
	 * @return the breakpoint's line number
	 */
	public int getLine();
	/**
	 * Sets a visual annotation corresponding to this 
	 * breakpoint.
	 * @param a
	 */
	public void setAnnotation(Annotation a);
	public Annotation getAnnotation();
	
}

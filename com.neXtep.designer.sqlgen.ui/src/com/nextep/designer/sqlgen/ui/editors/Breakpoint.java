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
package com.nextep.designer.sqlgen.ui.editors;

import org.eclipse.jface.text.source.Annotation;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.sqlgen.ui.services.IBreakpoint;

/**
 * A simple breakpoint implementation
 * 
 * @author Christophe Fondacci
 *
 */
public class Breakpoint implements IBreakpoint {
	private IPackage pkg;
	private int line;
	private Annotation annotation;
	public Breakpoint(IPackage pkg, int line) {
		this.pkg=pkg;
		this.line = line;
	}
	/**
	 * @see com.nextep.designer.sqlgen.ui.services.IBreakpoint#getLine()
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * @see com.nextep.designer.sqlgen.ui.services.IBreakpoint#getTarget()
	 */
	@Override
	public IPackage getTarget() {
		return pkg;
	}
	
	public void setAnnotation(Annotation a) {
		annotation = a;
	}
	/**
	 * @see com.nextep.designer.sqlgen.ui.services.IBreakpoint#getAnnotation()
	 */
	@Override
	public Annotation getAnnotation() {
		return annotation;
	}

}

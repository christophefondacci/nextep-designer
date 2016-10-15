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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.core.expressions.PropertyTester;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.NullGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.model.ITypedNode;

/**
 * Tests whether the receiver can be SQL generated or not.
 * 
 * @author Christophe Fondacci
 */
public class GenerablePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// We can only perform incremental generation for versionables so we fail the test if
		// currently tested object is not a versionable
		if ("isIncGenerable".equals(property)) {
			if (!(receiver instanceof IVersionable<?>)) {
				return false;
			}
		}
		if (receiver instanceof ITypedObject && !(receiver instanceof ITypedNode)
				&& !(receiver instanceof IBasicColumn) && !(receiver instanceof ISQLScript)) { // Added
																								// for
																								// bug
																								// DES-442
			ISQLGenerator g = GeneratorFactory.getGenerator((ITypedObject) receiver, DBGMHelper
					.getCurrentVendor());
			return g != null && !(g instanceof NullGenerator);
		}
		return false;
	}

}

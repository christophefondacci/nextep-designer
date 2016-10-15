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
package com.nextep.designer.sqlgen.postgre.generator;

import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.sqlgen.generic.generator.ConstraintGenerator;
import com.nextep.designer.sqlgen.model.IGenerationResult;

public class PostGreConstraintGenerator extends ConstraintGenerator {

	@Override
	protected String getConstraintNameBefore(IKeyConstraint c) {
		return "CONSTRAINT " + escape(c.getName()) + " "; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	protected String getConstraintNameAfter(IKeyConstraint c) {
		final StringBuilder buf = new StringBuilder();
		switch (c.getConstraintType()) {
		case UNIQUE:
		case PRIMARY:
			if (c instanceof IPhysicalObject) {
				final IPhysicalProperties physProps = ((IPhysicalObject) c).getPhysicalProperties();
				if (physProps != null) {
					ISQLGenerator generator = getGenerator(physProps.getType());
					if (generator != null) {
						final IGenerationResult result = generator.generateFullSQL(physProps);
						if (result != null) {
							buf.append(" USING INDEX "); //$NON-NLS-1$
							for (ISQLScript s : result.getAdditions()) {
								buf.append(s.getSql());
							}
						}
					}
				}
			}
		}
		return buf.toString();
	}
}

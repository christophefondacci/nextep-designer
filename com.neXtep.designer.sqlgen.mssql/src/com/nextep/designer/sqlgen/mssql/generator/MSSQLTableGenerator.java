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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.mssql.generator;

import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.generic.generator.TableGenerator;

/**
 * MSSQL extension to provide support for column alteration.
 * 
 * @author Christophe Fondacci
 */
public class MSSQLTableGenerator extends TableGenerator {

	@Override
	protected void addAlterColumnSQL(IBasicTable t, ISQLScript script,
			List<ISQLScript> columnUpdates) {
		for (ISQLScript s : columnUpdates) {
			script.appendSQL("ALTER TABLE ").appendSQL(escape(t.getName())).appendSQL(" "); //$NON-NLS-1$ //$NON-NLS-2$
			script.appendSQL(s.getSql());
			closeLastStatement(script);
		}
	}

	@Override
	protected String getCreateTable(IBasicTable t) {
		final String createTable = super.getCreateTable(t);
		if (t.isTemporary()) {
			return createTable + "##"; // Creating a global temporary table //$NON-NLS-1$
		} else {
			return createTable;
		}
	}
}

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
package com.nextep.designer.sqlgen.db2.generator;

import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.generic.generator.TableGenerator;

/**
 * This specific DB2 table generator adds support for DB2 column alteration by generating the
 * appropriate ALTER TABLE statement using underlying column generations.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DB2TableGenerator extends TableGenerator {

	@Override
	protected void addAlterColumnSQL(IBasicTable t, ISQLScript script,
			List<ISQLScript> columnUpdates) {
		final String tabName = t.getName();

		script.appendSQL("ALTER TABLE ").appendSQL(escape(tabName)).appendSQL(" "); //$NON-NLS-1$ //$NON-NLS-2$

		for (ISQLScript s : columnUpdates) {
			script.appendSQL(NEWLINE).appendSQL(INDENTATION);
			script.appendSQL(s.getSql());
		}
		closeLastStatement(script);

		// Handling DB2 need for REORG after column alteration
		script.appendSQL(prompt("Re-organizing table '" + tabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("CALL SYSPROC.ADMIN_CMD('REORG TABLE ").appendSQL(tabName) //$NON-NLS-1$
				.appendSQL("')"); //$NON-NLS-1$
		closeLastStatement(script);
	}

	@Override
	protected String getCreateTable(IBasicTable t) {
		if (t.isTemporary()) {
			return "DECLARE GLOBAL TEMPORARY TABLE "; //$NON-NLS-1$
		} else {
			return super.getCreateTable(t);
		}
	}
}

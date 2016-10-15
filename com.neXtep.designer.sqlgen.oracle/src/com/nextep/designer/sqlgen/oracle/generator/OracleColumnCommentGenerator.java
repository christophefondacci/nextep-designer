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
package com.nextep.designer.sqlgen.oracle.generator;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This generator generates column comments scripts.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleColumnCommentGenerator extends SQLGenerator {

	public OracleColumnCommentGenerator() {
		// Setting the DBVendor to ORACLE as this generator is only meant to be used for this
		// vendor.
		setVendor(DBVendor.ORACLE);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicColumn newColumn = (IBasicColumn) result.getSource();
		final IBasicColumn oldColumn = (IBasicColumn) result.getTarget();
		final String newColDesc = newColumn.getDescription();
		final String oldColDesc = oldColumn.getDescription();

		if (newColDesc != null && !"".equals(newColDesc.trim())) { //$NON-NLS-1$
			if (oldColumn == null || oldColDesc == null || !newColDesc.equals(oldColDesc)) {
				ISQLScript commentScript = getSqlScript(newColumn.getName() + " comments", "", //$NON-NLS-1$ //$NON-NLS-2$
						ScriptType.COMMENTS);
				commentScript.appendSQL("COMMENT ON COLUMN ").appendSQL(getDbRefName(newColumn)) //$NON-NLS-1$
						.appendSQL(" IS '").appendSQL(newColDesc.replace("'", "''")).appendSQL("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				closeLastStatement(commentScript);

				IGenerationResult genResult = GenerationFactory.createGenerationResult();
				genResult.addUpdateScript(new DatabaseReference(newColumn.getType(), "Comm " //$NON-NLS-1$
						+ getDbRefName(newColumn)), commentScript);
				return genResult;
			}
		}
		return null;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicColumn column = (IBasicColumn) model;
		final String colDesc = column.getDescription();

		if (colDesc != null && !"".equals(colDesc.trim())) { //$NON-NLS-1$
			ISQLScript commentScript = getSqlScript(column.getName() + " comments", "", //$NON-NLS-1$ //$NON-NLS-2$
					ScriptType.COMMENTS);
			commentScript.appendSQL("COMMENT ON COLUMN ").appendSQL(getDbRefName(column)) //$NON-NLS-1$
					.appendSQL(" IS '").appendSQL(colDesc.replace("'", "''")).appendSQL("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			closeLastStatement(commentScript);

			IGenerationResult genResult = GenerationFactory.createGenerationResult();
			genResult.addAdditionScript(new DatabaseReference(column.getType(), "Comm " //$NON-NLS-1$
					+ getDbRefName(column)), commentScript);
			return genResult;
		}
		return null;
	}

	/**
	 * Overriding to avoid going through drop strategies which would not work here
	 */
	@Override
	public IGenerationResult generateDrop(Object model) {
		return null;
	}

	public static String getDbRefName(IBasicColumn c) {
		return c.getParent().getName() + "." + c.getName(); //$NON-NLS-1$
	}

}

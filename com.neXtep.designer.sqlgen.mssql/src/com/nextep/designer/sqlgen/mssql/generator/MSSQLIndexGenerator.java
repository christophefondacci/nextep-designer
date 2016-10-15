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

import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * An extension of the default Index generator for MSSQL to handle drop syntax
 * 
 * @author Christophe Fondacci
 */
public class MSSQLIndexGenerator extends SQLGenerator {

	private ISQLGenerator jdbcGenerator;

	public MSSQLIndexGenerator() {
		jdbcGenerator = GeneratorFactory.getGenerator(IElementType.getInstance(IIndex.INDEX_TYPE),
				DBVendor.JDBC);
		jdbcGenerator.setVendor(DBVendor.MSSQL);
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		return jdbcGenerator.generateFullSQL(model);
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IIndex index = (IIndex) model;
		final String indexName = index.getIndexName();
		final ISQLScript dropScript = getSqlScript(indexName, index.getDescription(),
				ScriptType.INDEX);

		dropScript.appendSQL(prompt("Dropping index '" + indexName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("DROP INDEX ").appendSQL(escape(indexName)).appendSQL(" ON ") //$NON-NLS-1$ //$NON-NLS-2$
				.appendSQL(escape(index.getIndexedTable().getName()));
		closeLastStatement(dropScript);

		final IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult
				.addDropScript(new DatabaseReference(index.getType(), index.getName()), dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IGenerationResult r = GenerationFactory.createGenerationResult();
		r.integrate(doDrop(result.getTarget()));
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

}

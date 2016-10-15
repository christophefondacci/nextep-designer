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
package com.nextep.designer.sqlgen.generic.generator;

import java.util.Collection;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class IndexGenerator extends SQLGenerator {

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// TODO: Check if we are closer to mysql approach (need to drop /
		// recreate foreign keys) or
		// Oracle.
		// Uncomment the commented code for MySQL-like approach
		// Dropping the target
		IGenerationResult r = doDrop(result.getTarget()); // IndexGenerateFKDropsStrategy().generateDrop(this,result.getTarget());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		// Regenerating foreign keys
		// Collection<ForeignKeyConstraint> fkeys =
		// DBGMHelper.getForeignKeysForIndex((IIndex)result.getSource());
		// ISQLGenerator fkGenerator =
		// GeneratorFactory.getGenerator(IElementType.getInstance("FOREIGN_KEY"));
		// for(ForeignKeyConstraint fk : fkeys) {
		// r.integrate(fkGenerator.generateFullSQL(fk));
		// }
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IIndex index = (IIndex) model;
		final String indexName = getName(index.getIndexName(), index);

		ISQLScript s = new SQLScript(index.getIndexName(), index.getDescription(), "", //$NON-NLS-1$
				ScriptType.INDEX);
		s.appendSQL(prompt("Dropping index '" + index.getIndexName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		IGenerationResult r = GenerationFactory.createGenerationResult();
		IBasicTable parentTable = null;
		try {
			parentTable = index.getIndexedTable();
			r.addPrecondition(new DatabaseReference(parentTable.getType(), parentTable.getName()));
		} catch (ErrorException e) {
			s.setSql(prompt("Index '" + index.getIndexName() //$NON-NLS-1$
					+ "' not dropped because parent table has been dropped")); //$NON-NLS-1$
			r.addDropScript(new DatabaseReference(index.getType(), index.getName()), s);
			return r;
		}
		s.appendSQL("DROP INDEX " + escape(indexName)); //$NON-NLS-1$
		closeLastStatement(s);

		r.addDropScript(new DatabaseReference(index.getType(), index.getName()), s);
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IIndex index = (IIndex) model;
		final IBasicTable t = index.getIndexedTable();
		final String indexName = getName(index.getIndexName(), index);

		final ISQLScript s = new SQLScript(index.getIndexName(), index.getDescription(), "", //$NON-NLS-1$
				ScriptType.INDEX);

		s.appendSQL(prompt("Creating index '" + index.getIndexName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		s.appendSQL("CREATE "); //$NON-NLS-1$

		switch (index.getIndexType()) {
		case FULLTEXT:
		case UNIQUE:
		case SPATIAL:
			s.appendSQL(index.getIndexType().name() + " "); //$NON-NLS-1$
			break;
		}
		s.appendSQL("INDEX ").appendSQL(escape(indexName)).appendSQL(" ON ") //$NON-NLS-1$ //$NON-NLS-2$
				.appendSQL(escape(t.getName())).appendSQL(" (").appendSQL(NEWLINE); //$NON-NLS-1$
		boolean first = true;
		for (IBasicColumn c : index.getColumns()) {
			s.appendSQL("    "); //$NON-NLS-1$
			if (!first) {
				s.appendSQL(","); //$NON-NLS-1$
			} else {
				s.appendSQL(" "); //$NON-NLS-1$
				first = false;
			}
			s.appendSQL(escape(c.getName())).appendSQL(NEWLINE);
		}
		s.appendSQL(")"); //$NON-NLS-1$

		// Generating physical implementation when supported
		if (index instanceof IPhysicalObject) {
			final IPhysicalObject physIndex = (IPhysicalObject) index;
			final IPhysicalProperties props = physIndex.getPhysicalProperties();
			ISQLGenerator propGenerator = getGenerator(IElementType
					.getInstance(IIndexPhysicalProperties.TYPE_ID));
			if (propGenerator != null && props != null) {
				// Generating the physical implementation
				IGenerationResult propGeneration = propGenerator.generateFullSQL(props);
				// Aggregating implementation
				if (propGeneration != null) {
					s.appendSQL(" "); //$NON-NLS-1$
					Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
					for (ISQLScript physScript : generatedScripts) {
						s.appendScript(physScript);
					}
				}
			}
		}

		closeLastStatement(s);

		// Generating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(IElementType.getInstance(IIndex.INDEX_TYPE),
				index.getName()), s);
		// Adding a table precondition
		r.addPrecondition(new DatabaseReference(t.getType(), t.getName()));
		return r;
	}

}

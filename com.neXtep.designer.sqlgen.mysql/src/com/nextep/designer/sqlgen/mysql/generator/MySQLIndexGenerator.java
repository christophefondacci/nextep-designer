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
package com.nextep.designer.sqlgen.mysql.generator;

import java.util.Collection;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.mysql.strategies.IndexGenerateFKDropsStrategy;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class MySQLIndexGenerator extends MySQLGenerator {

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Dropping the target
		IDropStrategy dropStrategy = new IndexGenerateFKDropsStrategy();
		IGenerationResult r = dropStrategy.generateDrop(this, result.getTarget(), getVendor());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		// Regenerating foreign keys
		Collection<ForeignKeyConstraint> fkeys = DBGMHelper.getForeignKeysForIndex((IIndex) result
				.getSource());
		ISQLGenerator fkGenerator = getGenerator(IElementType.getInstance("FOREIGN_KEY"));
		for (ForeignKeyConstraint fk : fkeys) {
			r.integrate(fkGenerator.generateFullSQL(fk));
		}
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		IIndex index = (IIndex) model;
		ISQLScript s = new SQLScript(index.getIndexName(), index.getDescription(), "",
				ScriptType.INDEX);
		s.appendSQL("-- Dropping index '" + index.getIndexName() + "'..." + NEWLINE);
		IGenerationResult r = GenerationFactory.createGenerationResult();
		IBasicTable parentTable = null;
		try {
			parentTable = index.getIndexedTable();
			r.addPrecondition(new DatabaseReference(parentTable.getType(), parentTable.getName()));
		} catch (ErrorException e) {
			s.setSql("-- Index '" + index.getIndexName()
					+ "' not dropped because parent table has been dropped" + NEWLINE);
			r.addDropScript(new DatabaseReference(index.getType(), index.getName()), s);
			return r;
		}
		s.appendSQL("DROP INDEX " + escape(index.getIndexName()) + " ON "
				+ escape(parentTable.getName()) + NEWLINE + ";" + NEWLINE);

		r.addDropScript(new DatabaseReference(index.getType(), index.getName()), s);
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IIndex index = (IIndex) model;
		IMySQLIndex myIndex = null;
		if (index instanceof IMySQLIndex) {
			myIndex = (IMySQLIndex) index;
		}
		IBasicTable t = index.getIndexedTable();
		ISQLScript s = new SQLScript(index.getIndexName(), index.getDescription(), "",
				ScriptType.INDEX);
		s.appendSQL("-- Creating index '" + index.getIndexName() + "'..." + NEWLINE);
		s.appendSQL("CREATE ");
		switch (index.getIndexType()) {
		case FULLTEXT:
		case UNIQUE:
		case SPATIAL:
			s.appendSQL(index.getIndexType().name() + " ");
			break;
		}
		s.appendSQL("INDEX " + escape(index.getIndexName()) + " ON " + escape(t.getName()) + " ("
				+ NEWLINE);
		boolean first = true;
		for (IBasicColumn c : index.getColumns()) {
			s.appendSQL("    ");
			if (!first) {
				s.appendSQL(",");
			} else {
				s.appendSQL(" ");
				first = false;
			}
			s.appendSQL(escape(c.getName()));
			// MySQL specific prefix-length handling
			if (myIndex != null) {
				final Integer prefixLength = myIndex.getColumnPrefixLength(c.getReference());
				if (prefixLength != null) {
					s.appendSQL("(" + prefixLength + ")");
				}
			}
			s.appendSQL(NEWLINE);
		}
		s.appendSQL(")");
		if (index.getIndexType() == IndexType.HASH) {
			s.appendSQL(" USING HASH");
		}
		s.appendSQL(NEWLINE + ";" + NEWLINE);
		// Generating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(IElementType.getInstance(IIndex.INDEX_TYPE),
				index.getName()), s);
		// Adding a table precondition
		r.addPrecondition(new DatabaseReference(t.getType(), t.getName()));
		return r;
	}
}

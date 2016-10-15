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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * MySql generator for columns.<br>
 * Starting from 1.0.3, this generator will be invoked by JDBC workspaces so
 * implementation should never assume that the current generated column is a
 * {@link IMySQLColumn} and should always check the implementation before
 * accessing MySql-specific features.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLColumnGenerator extends MySQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(MySQLColumnGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicColumn c = (IBasicColumn) result.getSource();
		final IBasicColumn tgt = (IBasicColumn) result.getTarget();
		IGenerationResult r = GenerationFactory.createGenerationResult(c.getName());

		ISQLScript s = buildScript(c);
		if (isRenamed(result)) {
			s.setSql("CHANGE COLUMN " + escape(tgt.getName()) + " " + s.getSql()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			s.setSql("MODIFY COLUMN " + s.getSql()); //$NON-NLS-1$
		}
		if (result.getSource() != null && result.getTarget() != null) {
			IBasicColumn sourceCol = (IBasicColumn) result.getSource();
			IBasicColumn targetCol = (IBasicColumn) result.getTarget();
			if (targetCol.isNotNull() && !sourceCol.isNotNull()) {
				s.appendSQL(" NULL"); //$NON-NLS-1$
			}
			// Handling auto-increment, only for updates (else will be generated
			// from the primary
			// key)
			if (c instanceof IMySQLColumn && ((IMySQLColumn) c).isAutoIncremented()) {
				s.appendSQL(" AUTO_INCREMENT"); //$NON-NLS-1$
			}
		}
		r.addUpdateScript(new DatabaseReference(c.getType(), getDbRefName(c)), s);
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IBasicColumn c = (IBasicColumn) model;

		ISQLScript s = new SQLScript(c.getName(), c.getDescription(), "", ScriptType.TABLE); //$NON-NLS-1$
		s.appendSQL(prompt("Dropping column '" + c.getName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		s.appendSQL("ALTER TABLE ").appendSQL(escape(c.getParent().getName())) //$NON-NLS-1$
				.appendSQL(" DROP COLUMN ").appendSQL(escape(c.getName())); //$NON-NLS-1$
		closeLastStatement(s);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(c.getType(), getDbRefName(c)), s);

		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IBasicColumn c = (IBasicColumn) model;

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult(c.getName());
		r.addAdditionScript(new DatabaseReference(c.getType(), getDbRefName(c)), buildScript(c));
		return r;
	}

	private ISQLScript buildScript(IBasicColumn c) {
		final StringBuilder buf = new StringBuilder();
		final IDatatypeService datatypeService = CorePlugin.getService(IDatatypeService.class);
		final IDatatype columnType = datatypeService.getDatatype(DBVendor.MYSQL, c.getDatatype());

		// We check for any reserved word
		buf.append(escape(c.getName()) + " "); //$NON-NLS-1$
		buf.append(datatypeService.getDatatypeLabel(columnType, DBVendor.MYSQL));
		if (columnType.isUnsigned()) {
			if (datatypeService.getDatatypeProvider(DBVendor.MYSQL).listStringDatatypes()
					.contains(columnType.getName())) {
				LOGGER.warn("Ignored unsigned information from column " + getDbRefName(c)
						+ ": string datatypes cannot be unsigned");
			} else {
				buf.append(" UNSIGNED"); //$NON-NLS-1$
			}
		}

		// Handling character set
		if (c instanceof IMySQLColumn) {
			final IMySQLColumn mc = (IMySQLColumn) c;
			if (isCharsetDefinitionNeeded(mc)) {
				final String colCharset = mc.getCharacterSet();
				final String colCollation = mc.getCollation();
				buf.append(" CHARACTER SET " + colCharset); //$NON-NLS-1$
				if (!isEmpty(colCollation)) {
					buf.append(" COLLATE " + colCollation); //$NON-NLS-1$
				}
			}
		}

		// Handling nullity
		if (c.isNotNull()) {
			buf.append(" NOT NULL"); //$NON-NLS-1$
		} // else {
			// sqlText+=" NULL";
			// }
		if (!"".equals(c.getDefaultExpr()) && c.getDefaultExpr() != null) { //$NON-NLS-1$
			buf.append(" DEFAULT " + c.getDefaultExpr()); //$NON-NLS-1$
		}

		// Column comments
		if (!isEmpty(c.getDescription())) {
			buf.append(" COMMENT '" + getComment(c).replace("'", "''") + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		// Building script
		ISQLScript s = new SQLScript(c.getName(), c.getDescription(), buf.toString(),
				ScriptType.CUSTOM);
		return s;
	}

	private String getComment(INamedObject o) {
		final String desc = o.getDescription();
		if (desc.length() <= 60) {
			return desc;
		} else {
			return desc.substring(0, 60);
		}
	}

	private boolean isDefaultCollation(String charset, String collation) {
		if (!isEmpty(charset)) {
			final IMySqlModelService modelService = CorePlugin.getService(IMySqlModelService.class);
			final String defaultCollation = modelService.getDefaultCollation(charset);
			return defaultCollation.equals(collation);
		}
		return isEmpty(collation);
	}

	private boolean isCharsetDefinitionNeeded(IMySQLColumn mc) {
		final String colCharset = mc.getCharacterSet();
		String colCollation = mc.getCollation();
		if (isDefaultCollation(colCharset, colCollation)) {
			colCollation = null;
		}
		final IColumnable table = mc.getParent();
		// If everything is empty, we don't need any generation
		boolean needsCharsetGeneration = (!isEmpty(colCharset) || !isEmpty(colCollation));
		// otherwise...
		if (needsCharsetGeneration && (table instanceof IMySQLTable)) {
			final IMySQLTable t = (IMySQLTable) table;
			final String tableCharset = t.getCharacterSet();
			final String tableCollation = t.getCollation();

			if (!isEmpty(tableCharset) && !tableCharset.equals(colCharset)
					|| (isEmpty(tableCharset) && !isEmpty(colCharset))) {
				needsCharsetGeneration = true;
			} else if ((!isEmpty(tableCollation) && !tableCollation.equals(colCollation))
					|| (isEmpty(tableCollation) && !isEmpty(colCollation))) {
				needsCharsetGeneration = true;
			} else {
				needsCharsetGeneration = false;
			}
		}
		return needsCharsetGeneration;
	}

	public static String getDbRefName(IBasicColumn c) {
		return c.getParent().getName() + "." + c.getName(); //$NON-NLS-1$
	}

}

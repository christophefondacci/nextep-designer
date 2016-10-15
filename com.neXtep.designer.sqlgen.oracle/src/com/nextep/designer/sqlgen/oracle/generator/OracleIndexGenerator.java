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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * Oracle SQL Generator for indexes. <br>
 * <br>
 * Starting with 1.0.3 this generator is invoked by JDBC workspaces so
 * implementation should never assume that the current generated index is a
 * {@link IOracleIndex} and should always check the implementation before
 * accessing Oracle-specific features.
 * 
 * @author Christophe Fondacci
 */
public class OracleIndexGenerator extends SQLGenerator {

	private static final Log log = LogFactory.getLog(OracleIndexGenerator.class);

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		IIndex src = (IIndex) result.getSource();
		IIndex tgt = (IIndex) result.getTarget();

		// Shall we generate a rename statement ?
		if (isRenamedOnly(result)) {
			ISQLScript s = new SQLScript(src.getName(), src.getDescription(), "", ScriptType.INDEX);
			s.appendSQL("Prompt Renaming index '" + tgt.getIndexName() + "' to '"
					+ src.getIndexName() + "'..." + NEWLINE);
			s.appendSQL("ALTER INDEX " + getName(tgt.getIndexName(), src) + " RENAME TO "
					+ getName(src.getIndexName(), src) + NEWLINE + "/" + NEWLINE);
			IGenerationResult r = GenerationFactory.createGenerationResult();
			r.addUpdateScript(new DatabaseReference(src.getType(), src.getIndexName()), s);
			return r;
		}
		// Dropping the target
		IGenerationResult r = doDrop(result.getTarget());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		final IIndex index = (IIndex) model;
		final String indexName = getName(index.getIndexName(), index);
		final String rawName = index.getIndexName();

		ISQLScript s = new SQLScript(rawName, index.getDescription(), "", ScriptType.INDEX);
		s.appendSQL("Prompt Dropping index '" + rawName + "'..." + NEWLINE);
		s.appendSQL("DROP INDEX " + indexName + NEWLINE + "/" + NEWLINE);
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(index.getType(), rawName), s);
		try {
			final IBasicTable t = index.getIndexedTable();
			r.addPrecondition(new DatabaseReference(t.getType(), t.getName()));
		} catch (ErrorException e) {
			log.warn("Index '" + rawName + "' says his related table cannot be found...");
		}
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IIndex index = (IIndex) model;
		IOracleIndex oracleIndex = null;
		final String indexName = getName(index.getIndexName(), index);
		final String rawName = index.getIndexName();

		if (model instanceof IOracleIndex) {
			oracleIndex = (IOracleIndex) model;
		}
		IBasicTable t = index.getIndexedTable();
		final boolean isCluster = (t.getType() == IElementType
				.getInstance(IOracleCluster.CLUSTER_TYPE_ID));
		ISQLScript s = new SQLScript(rawName, index.getDescription(), "", ScriptType.INDEX);
		s.appendSQL("Prompt Creating index '" + rawName + "'..." + NEWLINE);
		s.appendSQL("CREATE ");
		switch (index.getIndexType()) {
		case BITMAP:
			s.appendSQL("BITMAP ");
			break;
		case UNIQUE:
			if (!isCluster) {
				s.appendSQL("UNIQUE ");
			}
			break;
		}
		s.appendSQL("INDEX " + indexName + " ON ");
		if (isCluster) {
			s.appendSQL("CLUSTER ");
			s.setScriptType(ScriptType.CLUSTER_INDEX);
		}
		s.appendSQL(getName(t) + " ");
		if (!isCluster) {
			s.appendSQL("(" + NEWLINE);
			boolean first = true;
			for (IBasicColumn c : index.getColumns()) {
				s.appendSQL("    ");
				if (!first) {
					s.appendSQL(",");
				} else {
					s.appendSQL(" ");
					first = false;
				}
				// Function based index generate function source here instead of
				// name
				String colExpr = null;
				if (oracleIndex != null) {
					colExpr = oracleIndex.getFunction(c.getReference());
				}
				if (colExpr != null && !colExpr.trim().isEmpty()) {
					s.appendSQL(colExpr + NEWLINE);
				} else {
					s.appendSQL(escape(c.getName()) + NEWLINE);
				}
			}
			s.appendSQL(") ");
		}
		// Adding physical properties, if any
		if (oracleIndex != null && oracleIndex.getPhysicalProperties() != null) {
			IPhysicalProperties props = oracleIndex.getPhysicalProperties();
			if (isCluster) {
				// Restricting to allowed cluster index attributes
				for (PhysicalAttribute a : new PhysicalAttribute[] { PhysicalAttribute.INIT_TRANS,
						PhysicalAttribute.MAX_TRANS, PhysicalAttribute.PCT_FREE }) {
					final Object val = props.getAttribute(a);
					if (val != null) {
						s.appendSQL(a.getName() + " " + val.toString() + " ");
					}
				}
				if (props.getTablespaceName() != null
						&& !props.getTablespaceName().trim().isEmpty()) {
					s.appendSQL("TABLESPACE " + props.getTablespaceName());
				}
				s.appendSQL(NEWLINE);
			} else {
				ISQLGenerator propGenerator = getGenerator(props.getType());
				if (propGenerator != null) {
					IGenerationResult propGeneration = propGenerator.generateFullSQL(props);
					if (propGeneration != null) {
						Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
						for (ISQLScript physScript : generatedScripts) {
							s.appendScript(physScript);
						}
					}
				}
			}
		} else {
			s.appendSQL(NEWLINE);
		}
		s.appendSQL("/" + NEWLINE);

		// Generating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(IElementType.getInstance(IIndex.INDEX_TYPE),
				rawName), s);
		// Adding a table precondition
		r.addPrecondition(new DatabaseReference(t.getType(), t.getName()));
		return r;
	}

}

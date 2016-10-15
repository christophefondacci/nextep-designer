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
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.impl.merge.TablePhysicalPropertiesMerger;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This generator is not declared as a SQLGenerator, it is only used internally by the
 * OracleTableGenerator.<br>
 * However, capitalization is made by extending the base class
 * {@link OraclePhysicalPropertiesGenerator} which contains base generation methods for
 * {@link IPhysicalProperties}
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTablePhysicalPropertiesGenerator extends OraclePhysicalPropertiesGenerator {

	private final ISQLGenerator oracleTablePartitionGenerator;

	public OracleTablePhysicalPropertiesGenerator() {
		this.oracleTablePartitionGenerator = new OracleTablePartitionGenerator(true);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IOracleTablePhysicalProperties src = (IOracleTablePhysicalProperties) result
				.getSource();
		if (src == null)
			return null;

		final IOracleTablePhysicalProperties tgt = (IOracleTablePhysicalProperties) result
				.getTarget();
		final IPhysicalObject table = src.getParent();
		final String tabName = table.getName();
		final String newTblspcName = src.getTablespaceName();

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();

		// Should we generate a ALTER TABLE ... MOVE clause?
		if (newTblspcName != null && !"".equals(newTblspcName.trim()) //$NON-NLS-1$
				&& !newTblspcName.equals(tgt.getTablespaceName())) {
			ISQLScript moveScript = getSqlScript(tabName, table.getDescription(), ScriptType.TABLE);

			// Move script which redefine all physical properties
			moveScript.appendSQL(prompt("Moving table '" + tabName + "' to tablespace " //$NON-NLS-1$ //$NON-NLS-2$
					+ newTblspcName + "...")); //$NON-NLS-1$
			moveScript.appendSQL("ALTER TABLE ").appendSQL(tabName).appendSQL(" MOVE "); //$NON-NLS-1$ //$NON-NLS-2$
			moveScript.appendScript(generateFullSQL(src).getAdditions().iterator().next());
			closeLastStatement(moveScript);

			genResult.addAdditionScript(new DatabaseReference(src.getType(), tabName), moveScript);
			genResult.addPrecondition(new DatabaseReference(table.getType(), tabName));

			// We have redefined everything so we should return here
			return genResult;
		}

		// Individual alter
		ISQLScript alterScript = getSqlScript(tabName, table.getDescription(), ScriptType.TABLE);

		alterScript.appendSQL(prompt("Modifying table '" + tabName + "' physical attributes...")); //$NON-NLS-1$ //$NON-NLS-2$
		alterScript.appendSQL("ALTER TABLE ").appendSQL(tabName).appendSQL(NEWLINE) //$NON-NLS-1$
				.appendSQL("    "); //$NON-NLS-1$
		boolean generateAlter = false;
		for (PhysicalAttribute a : PhysicalAttribute.values()) {
			if (appendAttribute(alterScript, src, tgt, a)) {
				generateAlter = true;
			}
		}
		closeLastStatement(alterScript);

		// Partitions diff generation
		IGenerationResult partGeneration = generateChildren(
				TablePhysicalPropertiesMerger.CATEGORY_PARTITIONS, result,
				oracleTablePartitionGenerator, false);
		genResult.integrate(partGeneration);

		if (generateAlter) {
			genResult.addUpdateScript(new DatabaseReference(src.getType(), tabName), alterScript);
		}

		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) model;
		final IGenerationResult r = super.generateFullSQL(model);
		final ISQLScript s = r.getAdditions().iterator().next();

		// We add the physical organization for index-organized tables.
		if (props.getPhysicalOrganisation() == PhysicalOrganisation.INDEX) {
			s.setSql("ORGANIZATION INDEX " + s.getSql()); //$NON-NLS-1$
		}
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

	@Override
	protected void appendCustomSQL(ISQLScript script, IPhysicalProperties props) {
		final IOracleTablePhysicalProperties tabProps = (IOracleTablePhysicalProperties) props;

		// Nothing to do if non-partitioned
		if (tabProps.getPartitioningMethod() == PartitioningMethod.NONE
				|| tabProps.getPartitioningMethod() == null) {
			return;
		}

		switch (tabProps.getPartitioningMethod()) {
		case RANGE:
			script.appendSQL("PARTITION BY RANGE ("); //$NON-NLS-1$
			break;
		case LIST:
			script.appendSQL("PARTITION BY LIST ("); //$NON-NLS-1$
			break;
		case HASH:
			script.appendSQL("PARTITION BY HASH ("); //$NON-NLS-1$
		}
		boolean first = true;

		// Partitioning columns declaration
		for (IReference r : tabProps.getPartitionedColumnsRef()) {
			IBasicColumn c = (IBasicColumn) VersionHelper.getReferencedItem(r);
			if (!first) {
				script.appendSQL(", "); //$NON-NLS-1$
			} else {
				first = false;
			}
			script.appendSQL(c.getName());
		}
		script.appendSQL(")  (").appendSQL(NEWLINE); //$NON-NLS-1$

		// Partitions definition
		IGenerationResult partGeneration = generateChildren(tabProps.getPartitions(), false);
		addCommaSeparatedScripts(script, "", ")" + NEWLINE, partGeneration.getAdditions()); //$NON-NLS-1$ //$NON-NLS-2$
	}

}

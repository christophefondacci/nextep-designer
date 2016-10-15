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
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.impl.merge.TablePartitionMerger;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTablePartitionGenerator extends SQLGenerator {

	private boolean generateAlterTable = false;

	public OracleTablePartitionGenerator() {
		// Setting the DBVendor of this generator to ORACLE as it is only meant to be used with this
		// vendor.
		setVendor(DBVendor.ORACLE);
	}

	public OracleTablePartitionGenerator(boolean generateAlterTable) {
		this();
		this.generateAlterTable = generateAlterTable;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final ITablePartition newPart = (ITablePartition) result.getSource();
		final String newPartName = newPart.getName();
		final ITablePartition oldPart = (ITablePartition) result.getTarget();
		final IBasicTable newPartTable = CorePlugin.getService(ICoreService.class)
				.getFirstTypedParent(newPart, IBasicTable.class);

		// Preparing result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();

		// Handling partition renaming
		if (isRenamed(result)) {
			ISQLScript alterScript = getSqlScript(newPartName, newPart.getDescription(),
					ScriptType.TABLE);
			alterScript.appendSQL(prompt("Renaming partition '" + newPartName + "' from table '" //$NON-NLS-1$ //$NON-NLS-2$
					+ newPartTable.getName() + "'...")); //$NON-NLS-1$
			alterScript.appendSQL("ALTER TABLE ").appendSQL(newPartTable.getName()) //$NON-NLS-1$
					.appendSQL(" RENAME PARTITION ").appendSQL(oldPart.getName()).appendSQL(" TO ") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(newPartName);
			closeLastStatement(alterScript);
			genResult.addUpdateScript(new DatabaseReference(newPart.getType(), newPartName),
					alterScript);
		}

		// Generating physical updates
		genResult.integrate(generateTypedChildren(TablePartitionMerger.ATTR_PHYSICALS, result,
				IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID), false));
		// Returning result
		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ITablePartition part = (ITablePartition) model;
		final String partName = part.getName();
		final IBasicTable partTable = CorePlugin.getService(ICoreService.class)
				.getFirstTypedParent(part, IBasicTable.class);

		ISQLScript dropScript = getSqlScript(partName, part.getDescription(), ScriptType.TABLE);
		dropScript.appendSQL(prompt("Dropping partition '" + partName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("ALTER TABLE ").appendSQL(partTable.getName()) //$NON-NLS-1$
				.appendSQL(" DROP PARTITION ").appendSQL(partName); //$NON-NLS-1$
		closeLastStatement(dropScript);

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(part.getType(), partName), dropScript);
		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ITablePartition part = (ITablePartition) model;
		final String partName = part.getName();
		final IOracleTablePhysicalProperties tabProps = (IOracleTablePhysicalProperties) part
				.getParent();
		final IBasicTable partTable = CorePlugin.getService(ICoreService.class)
				.getFirstTypedParent(part, IBasicTable.class);

		ISQLScript createScript = getSqlScript(partName, part.getDescription(), ScriptType.TABLE);
		if (generateAlterTable) {
			createScript.appendSQL(prompt("Adding partition '" + partName + "' to table '" //$NON-NLS-1$ //$NON-NLS-2$
					+ partTable.getName() + "'...")); //$NON-NLS-1$
			createScript.appendSQL("ALTER TABLE ").appendSQL(partTable.getName()).appendSQL(" ADD") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(NEWLINE);
		}
		createScript.appendSQL("     PARTITION ").appendSQL(partName).appendSQL(" "); //$NON-NLS-1$ //$NON-NLS-2$

		if (tabProps.getPartitioningMethod() != PartitioningMethod.HASH) {
			switch (tabProps.getPartitioningMethod()) {
			case RANGE:
				createScript.appendSQL("VALUES LESS THAN ("); //$NON-NLS-1$
				break;
			case LIST:
				createScript.appendSQL("VALUES ("); //$NON-NLS-1$
			}
			// We only support single column partitioning (and single values for lists)
			// so we simply add the value
			// TODO: iterate on values once supported
			createScript.appendSQL(part.getHighValue()).appendSQL(") "); //$NON-NLS-1$
		}

		// Adding partition physical properties
		if (part.getPhysicalProperties() != null) {
			IPhysicalProperties partProps = part.getPhysicalProperties();
			ISQLGenerator g = getGenerator(partProps.getType());
			IGenerationResult propGeneration = g.generateFullSQL(partProps);
			if (propGeneration != null) {
				Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
				for (ISQLScript s : generatedScripts) {
					createScript.appendScript(s);
				}
			}
		}

		if (generateAlterTable) {
			closeLastStatement(createScript);
		}

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(part.getType(), partName), createScript);
		return genResult;
	}

}

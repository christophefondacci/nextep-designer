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
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IIndexPartition;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.oracle.impl.merge.IndexPartitionMerger;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleIndexPartitionGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleIndexPartitionGenerator.class);

	private boolean generateAlterIndex = false;

	public OracleIndexPartitionGenerator() {
	}

	public OracleIndexPartitionGenerator(boolean generateAlterTable) {
		this.generateAlterIndex = generateAlterTable;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IIndexPartition newPart = (IIndexPartition) result.getSource();
		final String newPartName = newPart.getName();
		final IIndexPartition oldPart = (IIndexPartition) result.getTarget();
		final IIndex newPartIndex = CorePlugin.getService(ICoreService.class).getFirstTypedParent(
				newPart, IIndex.class);

		// Preparing result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();

		// Handling partition renaming
		if (isRenamed(result)) {
			ISQLScript alterScript = getSqlScript(newPartName, newPart.getDescription(),
					ScriptType.INDEX);
			alterScript.appendSQL(prompt("Renaming partition '" + newPartName + "' from index '" //$NON-NLS-1$ //$NON-NLS-2$
					+ newPartIndex.getName() + "'...")); //$NON-NLS-1$
			alterScript.appendSQL("ALTER INDEX " + newPartIndex.getName() + " RENAME PARTITION " //$NON-NLS-1$ //$NON-NLS-2$
					+ oldPart.getName() + " TO " + newPartName); //$NON-NLS-1$
			closeLastStatement(alterScript);
			genResult.addUpdateScript(new DatabaseReference(newPart.getType(), newPartName),
					alterScript);
		}

		// Generating physical updates
		genResult.integrate(generateTypedChildren(IndexPartitionMerger.ATTR_PHYSICALS, result,
				IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID), false));
		// Returning result
		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ITablePartition part = (ITablePartition) model;
		final String partName = part.getName();
		final IIndex index = CorePlugin.getService(ICoreService.class).getFirstTypedParent(part,
				IIndex.class);

		ISQLScript dropScript = getSqlScript(partName, part.getDescription(), ScriptType.DROP);
		dropScript.appendSQL(prompt("Dropping partition '" + partName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("ALTER INDEX " + index.getName() + " DROP PARTITION " + partName); //$NON-NLS-1$ //$NON-NLS-2$
		closeLastStatement(dropScript);

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(part.getType(), partName), dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IIndexPartition part = (IIndexPartition) model;
		final String partName = part.getName();
		final IIndex index = CorePlugin.getService(ICoreService.class).getFirstTypedParent(part,
				IIndex.class);

		if (generateAlterIndex) {
			LOGGER.warn("Oracle does not support additions of partitions on existing indexes ("
					+ index.getName() + "." + partName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
			// script.appendSQL("Prompt Adding partition '" + partName +"' to index '" +
			// index.getName() + "'..."+ NEWLINE);
			// script.appendSQL("ALTER INDEX " + index.getName() + " ADD" +
			// NEWLINE);
		}

		ISQLScript createScript = getSqlScript(partName, part.getDescription(), ScriptType.INDEX);
		createScript.appendSQL("     PARTITION " + partName + " "); //$NON-NLS-1$ //$NON-NLS-2$

		// Adding partition physical properties
		if (part.getPhysicalProperties() != null) {
			final IPhysicalProperties partProps = part.getPhysicalProperties();
			final ISQLGenerator g = getGenerator(partProps.getType());

			IGenerationResult propGeneration = g.generateFullSQL(partProps);
			if (propGeneration != null) {
				Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
				for (ISQLScript s : generatedScripts) {
					createScript.appendScript(s);
				}
			}
		}

		if (generateAlterIndex) {
			closeLastStatement(createScript);
		}

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(part.getType(), partName), createScript);
		return genResult;
	}

}

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

import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.impl.merge.IndexPhysicalPropertiesMerger;
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
public class OracleIndexPhysicalPropertiesGenerator extends OraclePhysicalPropertiesGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		IPhysicalProperties src = (IPhysicalProperties) result.getSource();
		IPhysicalProperties tgt = (IPhysicalProperties) result.getTarget();

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();

		// Should we generate a ALTER TABLE ... MOVE clause?
		if (src.getTablespaceName() != null && !"".equals(src.getTablespaceName().trim()) //$NON-NLS-1$
				&& !src.getTablespaceName().equals(tgt.getTablespaceName())) {
			ISQLScript moveScript = new SQLScript(ScriptType.INDEX);

			// Move script which redefine all physical properties
			moveScript.appendSQL(getSQLCommandWriter().promptMessage(
					"Moving Index '" + src.getParent().getName() + "' to tablespace " //$NON-NLS-1$ //$NON-NLS-2$
							+ src.getTablespaceName() + "...")); //$NON-NLS-1$
			moveScript.appendSQL("ALTER INDEX " + src.getParent().getName() + " REBUILD "); //$NON-NLS-1$ //$NON-NLS-2$
			moveScript.appendScript(generateFullSQL(src).getAdditions().iterator().next());
			moveScript.appendSQL(getSQLCommandWriter().closeStatement());

			r.addAdditionScript(new DatabaseReference(src.getType(), src.getParent().getName()),
					moveScript);
			r.addPrecondition(new DatabaseReference(src.getParent().getType(), src.getParent()
					.getName()));

			// We have redefined everything so we should return here
			return r;
		}

		// Individual alter
		ISQLScript alterScript = new SQLScript(ScriptType.TABLE);
		alterScript.appendSQL(getSQLCommandWriter().promptMessage(
				"Modifying Index '" + src.getParent().getName() + "' physical attributes...")); //$NON-NLS-1$ //$NON-NLS-2$
		alterScript.appendSQL("ALTER INDEX " + src.getParent().getName() + " " + NEWLINE + "    "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		boolean generateAlter = false;
		for (PhysicalAttribute a : PhysicalAttribute.values()) {
			if (appendAttribute(alterScript, src, tgt, a)) {
				generateAlter = true;
			}
		}
		alterScript.appendSQL(getSQLCommandWriter().closeStatement());

		// Partitions diff generation
		IGenerationResult partGeneration = generateChildren(
				IndexPhysicalPropertiesMerger.CATEGORY_PARTITIONS, result,
				new OracleIndexPartitionGenerator(true), false);
		r.integrate(partGeneration);

		if (generateAlter) {
			r.addUpdateScript(new DatabaseReference(src.getType(), src.getParent().getName()),
					alterScript);
		}
		return r;
	}

	@Override
	protected void appendCustomSQL(ISQLScript script, IPhysicalProperties props) {
		final IIndexPhysicalProperties indProps = (IIndexPhysicalProperties) props;

		// Nothing to do if non-partitioned
		if (indProps instanceof IPartitionable) {
			final IPartitionable partitionable = (IPartitionable) indProps;
			if (partitionable.getPartitioningMethod() == PartitioningMethod.NONE) {
				return;
			}

			script.appendSQL(" LOCAL (").appendSQL(NEWLINE); //$NON-NLS-1$

			IGenerationResult partGeneration = generateChildren(partitionable.getPartitions(),
					false);
			addCommaSeparatedScripts(script, "", ")" + NEWLINE, partGeneration.getAdditions()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

}

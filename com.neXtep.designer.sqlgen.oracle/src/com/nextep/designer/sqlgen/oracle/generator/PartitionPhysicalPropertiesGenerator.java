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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
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
public class PartitionPhysicalPropertiesGenerator extends OraclePhysicalPropertiesGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IPartitionPhysicalProperties src = (IPartitionPhysicalProperties) result.getSource();
		final IPartitionPhysicalProperties tgt = (IPartitionPhysicalProperties) result.getTarget();
		final IPartition part = CorePlugin.getService(ICoreService.class).getFirstTypedParent(src,
				IPartition.class);
		final IBasicTable partTable = CorePlugin.getService(ICoreService.class)
				.getFirstTypedParent(src, IBasicTable.class);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();

		// Should we generate a ALTER TABLE ... MOVE clause?
		if (src.getTablespaceName() != null && !"".equals(src.getTablespaceName().trim())
				&& !src.getTablespaceName().equals(tgt.getTablespaceName())) {
			ISQLScript moveScript = getSqlScript(part.getName(), part.getDescription(),
					ScriptType.TABLE);

			// Move script which redefine all physical properties
			moveScript.appendSQL(prompt("Moving partition '" + part.getName() + "' to tablespace "
					+ src.getTablespaceName() + "..."));
			moveScript.appendSQL("ALTER TABLE " + partTable.getName() + " MOVE PARTITION "
					+ part.getName() + NEWLINE);
			moveScript.appendScript(generateFullSQL(src).getAdditions().iterator().next());
			closeLastStatement(moveScript);
			r.addAdditionScript(new DatabaseReference(src.getType(), part.getName()), moveScript);
			r.addPrecondition(new DatabaseReference(part.getType(), part.getName()));
			// We have redefined everything so we should return here
			return r;
		}

		// Individual alter
		ISQLScript alterScript = new SQLScript(ScriptType.TABLE);
		alterScript.appendSQL(prompt("Modifying partition '" + part.getName()
				+ "' physical attributes..."));
		alterScript.appendSQL("ALTER TABLE " + partTable.getName() + " MODIFY PARTITION "
				+ part.getName() + NEWLINE + "    ");

		boolean generateAlter = false;
		for (PhysicalAttribute a : PhysicalAttribute.values()) {
			if (appendAttribute(alterScript, src, tgt, a)) {
				generateAlter = true;
			}
		}
		closeLastStatement(alterScript);

		if (generateAlter) {
			r.addUpdateScript(new DatabaseReference(src.getType(), part.getName()), alterScript);
			return r;
		} else {
			return null;
		}
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

}

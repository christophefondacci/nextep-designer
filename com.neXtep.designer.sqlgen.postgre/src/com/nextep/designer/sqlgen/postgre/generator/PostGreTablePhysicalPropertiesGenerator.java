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
package com.nextep.designer.sqlgen.postgre.generator;

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A SQL generator for PostgreSql table physical properties statements.
 * 
 * @author Christophe Fondacci
 */
public class PostGreTablePhysicalPropertiesGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ITablePhysicalProperties props = (ITablePhysicalProperties) model;

		final ISQLScript script = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		script.setScriptType(ScriptType.TABLE);
		script.appendSQL("TABLESPACE " + props.getTablespaceName()); //$NON-NLS-1$

		final IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(props.getType(), props.getParent().getName()),
				script);
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		// Nonsense, always null
		return null;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final ITablePhysicalProperties src = (ITablePhysicalProperties) result.getSource();
		final ITablePhysicalProperties tgt = (ITablePhysicalProperties) result.getTarget();

		if (src == null || src.getTablespaceName() == null
				|| (tgt != null && src.getTablespaceName().equals(tgt.getTablespaceName()))) {
			return null;
		}

		final ISQLScript script = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		script.setScriptType(ScriptType.TABLE);
		final String parentName = src.getParent().getName();
		script.appendSQL(prompt("Moving table " + parentName + " to tablespace " //$NON-NLS-1$ //$NON-NLS-2$
				+ src.getTablespaceName() + "...")); //$NON-NLS-1$
		script.appendSQL("ALTER TABLE " + escape(parentName) + " SET TABLESPACE " + src.getTablespaceName()); //$NON-NLS-1$ //$NON-NLS-2$
		closeLastStatement(script);

		final IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(src.getType(), parentName), script);
		r.addPrecondition(new DatabaseReference(src.getParent().getType(), parentName));
		return r;
	}

}

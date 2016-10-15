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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OraclePackageGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OraclePackageGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IPackage pkg = (IPackage) model;
		final String pkgName = getName(pkg);
		final String rawName = pkg.getName();

		// Building drop script
		ISQLScript drop = getSqlScript(pkgName, pkg.getDescription(), ScriptType.PACKAGE_BODY);
		drop.appendSQL(prompt("Dropping package '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		drop.appendSQL("DROP PACKAGE ").appendSQL(pkgName); //$NON-NLS-1$
		closeLastStatement(drop);

		// Building generation result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(pkg.getType(), rawName), drop);

		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IPackage pkg = (IPackage) model;
		final String pkgName = getName(pkg);
		final String rawName = pkg.getName();
		final String pkgDesc = pkg.getDescription();

		// Creating spec script
		ISQLScript spec = getSqlScript(pkgName, pkgDesc, ScriptType.PACKAGE_SPEC);
		spec.appendSQL(prompt("Creating package specification of '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

		// Replacing package name
		final String convertedSpec = pkg.getSpecSourceCode()
				.replaceFirst("(?i)" + rawName, pkgName);
		final String convertedBody = pkg.getBodySourceCode()
				.replaceFirst("(?i)" + rawName, pkgName);

		// Building SQL
		spec.appendSQL("CREATE OR REPLACE " + convertedSpec); //$NON-NLS-1$
		closeLastStatement(spec);
		spec.appendSQL("SHOW ERRORS").appendSQL(NEWLINE); //$NON-NLS-1$

		// Creating body script
		ISQLScript body = getSqlScript(rawName, pkgDesc, ScriptType.PACKAGE_BODY);
		body.appendSQL(prompt("Creating package body of '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

		body.appendSQL("CREATE OR REPLACE " + convertedBody); //$NON-NLS-1$
		closeLastStatement(body);
		body.appendSQL("SHOW ERRORS").appendSQL(NEWLINE); //$NON-NLS-1$

		// Creating wrapper
		SQLWrapperScript s = new SQLWrapperScript(rawName, pkgDesc);
		s.addChildScript(spec);
		if (pkg.getBodySourceCode() != null && !"".equals(pkg.getBodySourceCode().trim())) { //$NON-NLS-1$
			s.addChildScript(body);
		}

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(pkg.getType(), rawName), s);

		// Parsing references to generate package preconditions
		GenerationHelper.addSqlGenerationPreconditions(r, pkg.getName(), pkg,
				IElementType.getInstance(IPackage.TYPE_ID));

		return r;
	}

}

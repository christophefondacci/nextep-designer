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
package com.nextep.designer.unittest.sqlgen;

import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * @author Christophe Fondacci
 */
public class GenerationResultTest extends TestCase {

	private static final Log LOGGER = LogFactory.getLog(GenerationResultTest.class);

	@Override
	protected void runTest() throws Throwable {
		final IGenerationResult r = GenerationFactory.createGenerationResult();
		final ITypedObjectFactory factory = CorePlugin.getTypedObjectFactory();

		// Creating a 3-table script dependency
		final ISQLScript parentScript = factory.create(ISQLScript.class);
		final ISQLScript childScript = factory.create(ISQLScript.class);
		final ISQLScript childChildScript = factory.create(ISQLScript.class);

		parentScript.setSql("PARENT\n");
		parentScript.setName("PARENT");
		childScript.setSql("CHILD1\n");
		childScript.setName("CHILD1");
		childChildScript.setSql("CHILD_CHILD\n");
		childChildScript.setName("CHILD_CHILD");

		// Preparing generation results
		final IGenerationResult parentResult = GenerationFactory.createGenerationResult();
		final IGenerationResult childResult = GenerationFactory.createGenerationResult();
		final IGenerationResult childChildResult = GenerationFactory.createGenerationResult();

		// Adding scripts to individual results
		final IElementType tableType = IElementType.getInstance(IBasicTable.TYPE_ID);
		parentResult.addDropScript(new DatabaseReference(tableType, "PARENT"), parentScript);
		childResult.addDropScript(new DatabaseReference(tableType, "CHILD"), childScript);
		childChildResult.addDropScript(new DatabaseReference(tableType, "CHILD_CHILD"),
				childChildScript);

		// Defining dependencies
		childResult.addPrecondition(new DatabaseReference(tableType, "PARENT"));
		childChildResult.addPrecondition(new DatabaseReference(tableType, "CHILD"));
		childChildResult.addPrecondition(new DatabaseReference(tableType, "PARENT"));

		// Now integrating into master result
		r.integrate(childChildResult);
		r.integrate(parentResult);
		r.integrate(childResult);

		final List<ISQLScript> scripts = r.buildScript();
		final ISQLScript script = buildFlatScript(scripts);
		String sql = script.getSql();

		int childChildPos = sql.indexOf("CHILD_CHILD");
		int childPos = sql.indexOf("CHILD1");
		int parentPos = sql.indexOf("PARENT");

		LOGGER.info("Generation result script : ");
		LOGGER.info(sql);
		Assert.assertTrue("Child child script need to be dropped first (test is " + childChildPos
				+ " < " + childPos + " )", childChildPos < childPos);
		Assert.assertTrue("Child child script need to be dropped first (test is " + childChildPos
				+ " < " + parentPos + " )", childChildPos < parentPos);
		Assert.assertTrue("Child script need to be dropped second (test is " + childPos + " < "
				+ parentPos + " )", childPos < parentPos);
	}

	private ISQLScript buildFlatScript(List<ISQLScript> scripts) {
		final ITypedObjectFactory objectFactory = CorePlugin.getTypedObjectFactory();
		final ISQLScript flatScript = objectFactory.create(ISQLScript.class);
		for (ISQLScript script : scripts) {
			flatScript.appendScript(script);
		}
		return flatScript;
	}

	public String getName() {
		return "Generation result test";
	};
}

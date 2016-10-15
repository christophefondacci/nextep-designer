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
package com.nextep.designer.unittest.dbgm;

import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Bruno Gautier
 */
public class SynonymTest extends VersionableTestCase {

	private static final String SYNONYM_NAME_NOFORMAT = " syn emp ";
	private static final String SYNONYM_NAME_FORMATTED = "SYN_EMP";

	private static final String REF_OBJ_NAME_NOFORMAT = " employees ";
	private static final String REF_OBJ_NAME_FORMATTED = "EMPLOYEES";

	private static final String REF_OBJ_SCHEMA_NOFORMAT = " my schema ";
	private static final String REF_OBJ_SCHEMA_FORMATTED = "MY_SCHEMA";

	private static final String REF_OBJ_DBLINK_NOFORMAT = " remote db ";
	private static final String REF_OBJ_DBLINK_FORMATTED = "REMOTE_DB";

	@Override
	protected void runTest() throws Throwable {
		// Testing versionable behavior
		IVersionable<ISynonym> vSynonym = VersionableFactory.createVersionable(ISynonym.class);
		assertNotNull("Failed to create versionable from factory", vSynonym);

		final ISynonym synonym = (ISynonym) vSynonym.getVersionnedObject().getModel();
		try {
			synonym.checkConsistency();
			fail("Failed to check synonym consistency");
		} catch (InconsistentObjectException ioe) {
			// Expected behavior: to be consistent, a Synonym must have a name and a referred object
			// name set.
		}

		vSynonym.setName(SYNONYM_NAME_NOFORMAT);
		assertTrue("Failed to set versionable name",
				((vSynonym.getName() != null) && (!"".equals(vSynonym.getName()))));
		assertEquals("Failed to format versionable name", vSynonym.getName(),
				SYNONYM_NAME_FORMATTED);

		vSynonym.setDescription("This is a synonym for TAB_EMPLOYEES table");
		assertTrue("Failed to set versionable description",
				((vSynonym.getDescription() != null) && (!"".equals(vSynonym.getDescription()))));

		// Testing the synonym controller
		@SuppressWarnings("unused")
		ITypedObjectUIController synCtrl = testController(vSynonym, ISynonym.TYPE_ID);

		// Testing synonym behavior
		try {
			synonym.checkConsistency();
			fail("Failed to check synonym consistency");
		} catch (InconsistentObjectException ioe) {
			// Expected behavior: to be consistent, a Synonym must have a name and a referred object
			// name set.
		}

		synonym.setRefDbObjName(REF_OBJ_NAME_NOFORMAT);
		assertTrue("Failed to set synonym referred object name",
				((synonym.getRefDbObjName() != null) && (!"".equals(synonym.getRefDbObjName()))));
		assertEquals("Failed to format synonym referred object name", synonym.getRefDbObjName(),
				REF_OBJ_NAME_FORMATTED);

		try {
			synonym.checkConsistency();
		} catch (InconsistentObjectException ioe) {
			fail("Failed to check synonym consistency");
		}

		// Now we can safely save the synonym since it is consistent
		IVersionContainer parent = getFirstContainer();
		vSynonym.setContainer(parent);
		CorePlugin.getIdentifiableDao().save(vSynonym);
		parent.addVersionable(vSynonym, new ImportPolicyAddOnly());

		synonym.setRefDbObjSchemaName(REF_OBJ_SCHEMA_NOFORMAT);
		assertTrue("Failed to set synonym referred object schema name",
				((synonym.getRefDbObjSchemaName() != null) && (!"".equals(synonym
						.getRefDbObjSchemaName()))));
		assertEquals("Failed to format synonym referred object schema name",
				synonym.getRefDbObjSchemaName(), REF_OBJ_SCHEMA_FORMATTED);

		// Testing oracle synonym behavior
		final IOracleSynonym oraSynonym = (IOracleSynonym) vSynonym.getVersionnedObject()
				.getModel();

		oraSynonym.setPublic(true);
		assertTrue("Failed to set Oracle synonym accessibility", oraSynonym.isPublic());

		oraSynonym.setRefDbObjDbLinkName(REF_OBJ_DBLINK_NOFORMAT);
		assertTrue("Failed to set Oracle synonym referred object DBLink name",
				((oraSynonym.getRefDbObjDbLinkName() != null) && (!"".equals(oraSynonym
						.getRefDbObjDbLinkName()))));
		assertEquals("Failed to format Oracle synonym referred object DBLink name",
				oraSynonym.getRefDbObjDbLinkName(), REF_OBJ_DBLINK_FORMATTED);

		// [CFO]: Adjusting synonym to be consistent with workspace
		oraSynonym.setRefDbObjDbLinkName("");
		oraSynonym.setRefDbObjSchemaName("");
		oraSynonym.setPublic(false);
		// Testing versionable versioning behavior
		testVersioning(vSynonym);
	}

	@Override
	public String getName() {
		return "Synonym creation / versioning";
	}

}

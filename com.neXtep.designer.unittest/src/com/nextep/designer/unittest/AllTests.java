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
package com.nextep.designer.unittest;

import java.util.Map;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.unittest.core.ListenerServiceTest;
import com.nextep.designer.unittest.core.ReferenceTest;
import com.nextep.designer.unittest.dbgm.merge.TableMergerTest;
import com.nextep.designer.unittest.sqlgen.GenerationResultTest;
import com.nextep.designer.unittest.synch.SynchronizationTest;
import com.nextep.designer.unittest.vcs.ImportPolicyTest;
import com.nextep.designer.unittest.vcs.WorkspaceChangeTest;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * neXtep designer tests implemented here are not unit tests!<br>
 * <br>
 * Instead, they are meant to test functional parts of the neXtep designer environment. For example,
 * we will test every versioning scenario for all {@link IVersionable} types which involves many
 * parts of the neXtep framework (such as listeners, reference management, factories, instance copy,
 * instance comparison). <br>
 * Since they are functional tests, they are linked together so please do neither ordering change
 * nor remove a test case. <br>
 * These tests are needed to provide a security harness against regression (this is the true enemy).
 * Since tests haven't been implemented from the beginning (shame on me, but time restrictions went
 * first) this test suite aims to cover the largest functional parts of the product. <br>
 * <b>Please implement true unit tests in distinct plugin (1 test plugin for 1 neXtep plugin) to
 * test individual classes</b>
 * 
 * @author Christophe
 */
public class AllTests {

	static Map<DBVendor, Test> vendorTests = new TreeMap<DBVendor, Test>();

	static {
		vendorTests.put(DBVendor.ORACLE, new OracleTestSuite());
		vendorTests.put(DBVendor.MYSQL, new MySqlTestSuite());
		vendorTests.put(DBVendor.POSTGRE, new CommonTestSuite());
		vendorTests.put(DBVendor.DB2, new CommonTestSuite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.nextep.designer.unittest");
		Designer.setUnitTest(true);
		suite.addTest(new ImportPolicyTest());
		suite.addTest(new ListenerServiceTest());
		// $JUnit-BEGIN$
		suite.addTest(new ReferenceTest());
		suite.addTest(new GenerationResultTest());

		for (DBVendor v : vendorTests.keySet()) {
			suite.addTest(new WorkspaceChangeTest(v));
			final Test test = vendorTests.get(v);
			suite.addTest(test);
			suite.addTest(new SynchronizationTest());
			suite.addTest(new TableMergerTest());
			suite.addTest(new GenerationResultTest());
		}

		// FIXME [BGA]: This Junit3 test suite is not compatible with Junit4 test case. Either the
		// existing test cases should be migrated to Junit4, or the Junit4 test cases execution
		// should be delegated to a specific test suite.
		// suite.addTest(new ObservableTest());

		// $JUnit-END$
		return suite;
	}

}

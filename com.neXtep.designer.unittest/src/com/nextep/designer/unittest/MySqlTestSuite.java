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

import junit.framework.TestSuite;
import com.nextep.designer.unittest.dbgm.DataSetTest;
import com.nextep.designer.unittest.dbgm.IndexTest;
import com.nextep.designer.unittest.dbgm.StorageServiceTest;
import com.nextep.designer.unittest.dbgm.ViewTest;
import com.nextep.designer.unittest.dbgm.mysql.MySqlBasicTableTest;
import com.nextep.designer.unittest.dbgm.mysql.MySqlProcedureTest;
import com.nextep.designer.unittest.vcs.ContainerComparisonTest;
import com.nextep.designer.unittest.vcs.ContainerTest;

public class MySqlTestSuite extends TestSuite {

	public MySqlTestSuite() {
		addTest(new ContainerTest());
		addTest(new MySqlBasicTableTest());
		addTest(new DataSetTest());
		addTest(new StorageServiceTest());
		addTest(new ContainerComparisonTest());
		addTest(new IndexTest());
		addTest(new ViewTest());
		addTest(new MySqlProcedureTest());
	}
}

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
package com.nextep.designer.testing.model;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * This interface defines a test for compatibility
 * @author Christophe
 */
public interface ICompatibilityTest {

	/**
	 * Defines the release to check compatibility with. The specified
	 * release should be a container release.
	 * 
	 * @param version compatible release to check
	 */
	public void setCompatilibityRelease(IVersionInfo version);
	/**
	 * @return the compatibility release 
	 */
	public IVersionInfo getCompatibilityRelease();
	/**
	 * Defines the current container release which will be tested.
	 * 
	 * @param version release to test
	 */
	public void setContainer(IVersionContainer version);
	/**
	 * @return the current container which should be tested
	 */
	public IVersionContainer getContainer();
	/**
	 * Defines the source connection to use for compatibility check.
	 * @param conn database target connection
	 */
	public void setConnection(IConnection conn);
	/**
	 * @return the database connection
	 */
	public IConnection getConnection();
	/**
	 * Defines the listener which will handle testing events
	 * 
	 * @param eventListener event handler implementation
	 */
	public void setEventHandler(ITestEventHandler eventListener);
	/**
	 * Runs the current test
	 * @return the status for this test
	 */
	public TestStatus run(IProgressMonitor monitor);
	/**
	 * @return the name of this test
	 */
	public String getName();

	
}

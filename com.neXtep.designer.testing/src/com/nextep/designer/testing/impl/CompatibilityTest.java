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
package com.nextep.designer.testing.impl;

import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.testing.model.ICompatibilityTest;
import com.nextep.designer.testing.model.ITestEventHandler;
import com.nextep.designer.testing.model.TestEvent;
import com.nextep.designer.testing.model.TestStatus;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

public abstract class CompatibilityTest implements ICompatibilityTest {

	private IVersionContainer container;
	private IVersionInfo compatibilityRelease;
	private IConnection connection;
	private ITestEventHandler eventHandler;
	@Override
	public IVersionInfo getCompatibilityRelease() {
		return compatibilityRelease;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public void setCompatilibityRelease(IVersionInfo version) {
		this.compatibilityRelease = version;
	}

	@Override
	public void setConnection(IConnection conn) {
		this.connection = conn;
	}

	@Override
	public void setEventHandler(ITestEventHandler eventListener) {
		this.eventHandler=eventListener;
	}

	protected ITestEventHandler getHandler() {
		return eventHandler;
	}
	
	@Override
	public IVersionContainer getContainer() {
		return container;
	}
	@Override
	public void setContainer(IVersionContainer container) {
		this.container=container;
	}
	/**
	 * Dispatches handling to the handler on the UI thread.
	 * 
	 * @param t
	 * @param e
	 * @param s
	 * @param o
	 */
	protected void handle(final ITypedObject t, final TestEvent e, final TestStatus s, final Object o) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getHandler().handle(t, e,s,o);
			}
		});
	}
}

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
package com.nextep.designer.splashHandlers;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Christophe Fondacci
 */
public class SplashProgressMonitor implements IProgressMonitor {

	private IProgressMonitor monitor;
	private boolean hasBegun;

	public SplashProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void beginTask(String name, int totalWork) {
		// This wrapper's only purpose is to prevent the Eclipse beginTask call which would
		// reinitialize the progress bar with 25 remaining work. As we have already started to
		// report
		// some progress and because we need way more ticks, we skip it
		if (!hasBegun) {
			monitor.beginTask(name, totalWork);
			hasBegun = true;
		}

	}

	public void done() {
		monitor.done();
	}

	public void internalWorked(double work) {
		monitor.internalWorked(work);
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		monitor.setCanceled(value);
	}

	public void setTaskName(String name) {
		monitor.setTaskName(name);
	}

	public void subTask(String name) {
		monitor.subTask(name);
	}

	public void worked(int work) {
		monitor.worked(work);
	}

}
